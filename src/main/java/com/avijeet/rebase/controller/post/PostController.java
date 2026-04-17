package com.avijeet.rebase.controller.post;

import com.avijeet.rebase.config.auth.AuthenticatedUser;
import com.avijeet.rebase.dto.post.CommentResponse;
import com.avijeet.rebase.dto.post.CreateCommentRequest;
import com.avijeet.rebase.dto.post.CreatePostRequest;
import com.avijeet.rebase.dto.post.LgtmResponse;
import com.avijeet.rebase.dto.post.PostResponse;
import com.avijeet.rebase.service.post.CommentService;
import com.avijeet.rebase.service.post.PostService;
import com.avijeet.rebase.utils.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(com.avijeet.rebase.utils.constants.ApiConstants.API_BASE_URL + "/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post feed, reactions, and comments")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create post", description = "Creates a post with optional image or code snippet")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PostResponse> createPost(
            Authentication authentication,
            @RequestPart("request") @Valid CreatePostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.success("Post created successfully",
                postService.createPost(principal, request, image));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get feed", description = "Returns paginated feed for authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Page<PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ApiResponse.success("Feed fetched successfully",
                postService.getFeed(PageRequest.of(page, size)));
    }

    @PostMapping("/{postId}/lgtm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Toggle LGTM", description = "Adds or removes current user's LGTM on a post")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<LgtmResponse> toggleLgtm(
            Authentication authentication,
            @PathVariable Long postId) {

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.success("Lgtm toggled", postService.toggleLgtm(principal, postId));
    }

    @GetMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get comments", description = "Returns paginated comments for a post")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ApiResponse.success("Comments fetched successfully",
                commentService.getComments(postId, PageRequest.of(page, size)));
    }

    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Add comment",
            description = "Creates a comment from JSON body or content query param"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Comment validation failed")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Optional JSON body when content is not sent as query param",
            content = @Content(schema = @Schema(implementation = CreateCommentRequest.class))
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CommentResponse> addComment(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam(required = false) String content,
            @RequestBody(required = false) CreateCommentRequest body) {

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        String commentContent = (body != null && body.content() != null) ? body.content() : content;
        if (commentContent == null || commentContent.isBlank()) {
            throw new com.avijeet.rebase.exceptions.InvalidArgumentsException("Comment content must not be blank");
        }
        return ApiResponse.success("Comment added",
                commentService.addComment(principal, postId, new CreateCommentRequest(commentContent)));
    }
}