package umc.todaynan.converter;

import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

public class PostCommentConverter {
    public static PostComment toPostComment(PostRequestDTO.CreatePostCommentDTO request, Integer maxBundleId, Post post, User user) {
        return PostComment.builder()
                .comment(request.getComment())
                .depth(0L)
                .parentComment(null)
                .childComments(null)
                .bundleId(maxBundleId != null ? maxBundleId + 1L : 1L)
                .post(post)
                .user(user)
                .build();
    }

    public static PostComment toPostChildComment(PostRequestDTO.CreatePostCommentDTO request,Post post, User user, PostComment parentComment) {
        return PostComment.builder()
                .comment(request.getComment())
                .depth(parentComment.getDepth()+1)
                .parentComment(parentComment)
                .childComments(null)
                .bundleId(parentComment.getBundleId())
                .post(post)
                .user(user)
                .build();
    }

    public static PostResponseDTO.CreatePostCommentResultDTO toCreateResultDTO(PostComment postComment) {
        return PostResponseDTO.CreatePostCommentResultDTO.builder()
                .post_comment_id(postComment.getId())
                .post_id(postComment.getPost().getId())
                .user_id(postComment.getUser().getId())
                .comment(postComment.getComment())
                .build();
//        return null;
    }

    public static PostResponseDTO.UpdatePostCommentResultDTO toUpdateResultDTO(PostComment postComment) {
        return PostResponseDTO.UpdatePostCommentResultDTO.builder()
                .post_comment_id(postComment.getId())
                .post_id(postComment.getPost().getId())
                .user_id(postComment.getUser().getId())
                .comment(postComment.getComment())
                .build();
        //        return null;
    }

    public static PostResponseDTO.LikePostCommentResultDTO toLikeResultDTO(PostCommentLike  postCommentLike) {
        return PostResponseDTO.LikePostCommentResultDTO.builder()
                .post_comment_like_id(postCommentLike.getId())
                .post_comment_id(postCommentLike.getPostComment().getId())
                .user_id(postCommentLike.getUser().getId())
                .build();
        //        return null;
    }
}
