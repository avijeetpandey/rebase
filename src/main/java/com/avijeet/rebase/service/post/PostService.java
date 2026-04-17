package com.avijeet.rebase.service.post;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.post.CreatePostRequest;
import com.avijeet.rebase.dto.post.PostAuthorResponse;
import com.avijeet.rebase.dto.post.PostResponse;
import com.avijeet.rebase.entities.Post;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.entities.UserProfile;
import com.avijeet.rebase.exceptions.InvalidArgumentsException;
import com.avijeet.rebase.repository.post.PostRepository;
import com.avijeet.rebase.repository.profile.UserProfileRepository;
import com.avijeet.rebase.repository.user.UserRepository;
import com.avijeet.rebase.service.minio.MinioService;
import com.avijeet.rebase.dto.post.LgtmResponse;
import com.avijeet.rebase.entities.PostLgtm;
import com.avijeet.rebase.exceptions.ResourceNotFoundException;
import com.avijeet.rebase.repository.post.PostLgtmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MinioService minioService;
    private final PostLgtmRepository postLgtmRepository;

    @Transactional
    public PostResponse createPost(AuthenticatedUser principal, CreatePostRequest request, MultipartFile image) {
        boolean hasCodeSnippet = StringUtils.hasText(request.codeSnippet());
        boolean hasImage = image != null && !image.isEmpty();

        if (hasCodeSnippet && hasImage) {
            log.warn("userId: {} attempted to upload both image and code snippet", principal.userId());
            throw new InvalidArgumentsException("A post can contain either an image or a code snippet, not both.");
        }

        User user = userRepository.getReferenceById(principal.userId()); // Proxy is enough to set relation
        Post post = new Post();
        post.setUser(user);
        post.setContent(request.content());

        if (hasCodeSnippet) {
            post.setCodeSnippet(request.codeSnippet());
            post.setCodeLanguage(request.codeLanguage());
        }

        if (hasImage) {
            String imageUrl = minioService.uploadFile(image, "posts");
            post.setImageUrl(imageUrl);
        }

        Post savedPost = postRepository.save(post);
        log.info("Post created by userId: {}, postId: {}", principal.userId(), savedPost.getId());

        return mapToResponse(savedPost, getAuthorAvatar(principal.userId()));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> mapToResponse(post, getAuthorAvatar(post.getUser().getId())));
    }

    private String getAuthorAvatar(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getAvatarUrl)
                .orElse(null);
    }

    @Transactional
    public LgtmResponse toggleLgtm(AuthenticatedUser principal, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        boolean alreadyLgtmed = postLgtmRepository.existsByPostIdAndUserId(postId, principal.userId());

        if (alreadyLgtmed) {
            postLgtmRepository.findByPostIdAndUserId(postId, principal.userId())
                    .ifPresent(postLgtmRepository::delete);
            post.setLgtmCount(Math.max(0, post.getLgtmCount() - 1));
        } else {
            PostLgtm lgtm = new PostLgtm();
            lgtm.setPost(post);
            lgtm.setUser(userRepository.getReferenceById(principal.userId()));
            postLgtmRepository.save(lgtm);
            post.setLgtmCount(post.getLgtmCount() + 1);
        }

        postRepository.save(post);
        log.info("Lgtm toggled by userId: {} on postId: {} -> lgtmed: {}", principal.userId(), postId, !alreadyLgtmed);
        return new LgtmResponse(post.getLgtmCount(), !alreadyLgtmed);
    }

    private PostResponse mapToResponse(Post post, String avatarUrl) {        PostAuthorResponse authorResponse = new PostAuthorResponse(
                post.getUser().getId(),
                post.getUser().getUsername(),
                avatarUrl
        );
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getImageUrl(),
                post.getCodeSnippet(),
                post.getCodeLanguage(),
                post.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                post.getLgtmCount(),
                authorResponse
        );
    }
}
