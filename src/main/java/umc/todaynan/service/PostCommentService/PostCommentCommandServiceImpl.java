package umc.todaynan.service.PostCommentService;

import jakarta.servlet.http.HttpServletRequest;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;

import java.util.List;

public interface PostCommentCommandServiceImpl {
    public PostComment createComment(Long post_id, Long comment_id, PostRequestDTO.CreatePostCommentDTO request, HttpServletRequest httpServletRequest);
    public PostComment updateComment(Long post_id, Long comment_id, PostRequestDTO.UpdatePostCommentDTO request, HttpServletRequest httpServletRequest);
    public Boolean deleteComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest);
    public PostCommentLike likeComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest);
    public List<PostComment> getPostCommentList(Long post_id, HttpServletRequest httpServletRequest);
}
