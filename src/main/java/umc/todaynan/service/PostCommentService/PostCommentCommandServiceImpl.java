package umc.todaynan.service.PostCommentService;

import jakarta.servlet.http.HttpServletRequest;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.util.List;

public interface PostCommentCommandServiceImpl {
    PostResponseDTO.CreatePostCommentResultDTO createComment(Long post_id, Long comment_id, PostRequestDTO.CreatePostCommentDTO request, HttpServletRequest httpServletRequest);
    PostResponseDTO.UpdatePostCommentResultDTO updateComment(Long post_id, Long comment_id, PostRequestDTO.UpdatePostCommentDTO request, HttpServletRequest httpServletRequest);
    String deleteComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest);
    PostResponseDTO.LikePostCommentResultDTO likeComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest);
    List<PostComment> getPostCommentList(Long post_id, HttpServletRequest httpServletRequest);
}
