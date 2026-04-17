package com.avijeet.rebase.service.post;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.post.CommentAuthorResponse;
import com.avijeet.rebase.dto.post.CommentResponse;
import com.avijeet.rebase.dto.post.CreateCommentRequest;
import com.avijeet.rebase.entities.Comment;
import com.avijeet.rebase.entities.Post;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.entities.UserProfile;
import com.avijeet.rebase.exceptions.ResourceNotFoundException;
import com.avijeet.rebase.repository.post.CommentRepository;
import com.avijeet.rebase.repository.post.PostRepository;
import com.avijeet.rebase.repository.profile.UserProfileRepository;
import com.avijeet.rebase.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public CommentResponse addComment(AuthenticatedUser principal, Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        User user = userRepository.getReferenceById(principal.userId());

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.content());

        Comment saved = commentRepository.save(comment);
        log.info("Comment added by userId: {} on postId: {}", principal.userId(), postId);
        return mapToResponse(saved);
    }

    private CommentResponse mapToResponse(Comment comment) {
        String avatarUrl = userProfileRepository.findByUserId(comment.getUser().getId())
                .map(UserProfile::getAvatarUrl)
                .orElse(null);

        CommentAuthorResponse author = new CommentAuthorResponse(
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                avatarUrl
        );

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                author
        );
    }
}

