package umc.todaynan.converter;

import org.springframework.data.domain.Page;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    public static PostResponseDTO.MyPostCommentListDTO toPostCommentListDTO(Page<PostComment> postComments) {
        List<PostResponseDTO.PostCommentDTO> postDTOList = postComments.stream()
                .map(PostCommentConverter::toPostCommentDTO).collect(Collectors.toList());

        return PostResponseDTO.MyPostCommentListDTO.builder()
                .isLast(postComments.isLast())
                .isFirst(postComments.isFirst())
                .totalPage(postComments.getTotalPages())
                .totalElements(postComments.getTotalElements())
                .listSize(postComments.getSize())
                .postCommentList(postDTOList)
                .build();
    }

    public static PostResponseDTO.PostCommentDTO toPostCommentDTO(PostComment postComment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String formattedCreatedAt = postComment.getCreatedAt().format(formatter);

        return PostResponseDTO.PostCommentDTO.builder()
                .postId(postComment.getPost().getId())
                .userId(postComment.getUser().getId())
                .content(postComment.getComment())
                .commentLike(postComment.getPostCommentLikes().size())
                .createdAt(formattedCreatedAt)
                .build();
    }


}
