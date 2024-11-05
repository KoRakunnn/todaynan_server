package umc.todaynan.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

public interface PostService {
    PostResponseDTO.CreatePostResultDTO createPost(PostRequestDTO.CreatePostDTO request, HttpServletRequest httpServletRequest);
    PostResponseDTO.UpdatePostResultDTO updatePost(Long post_id, PostRequestDTO.UpdatePostDTO request, HttpServletRequest httpServletRequest);
    String deletePost(Long post_id, HttpServletRequest httpServletRequest);
    PostResponseDTO.LikePostResultDTO likePost(Long post_id, HttpServletRequest httpServletRequest);
    PostResponseDTO.PostDetailResultDTO getPostDetail(Long post_id, HttpServletRequest httpServletRequest);

    PostResponseDTO.PostListDTO getEmployPostList(HttpServletRequest request, Integer page);
    PostResponseDTO.PostListDTO getChatPostList(HttpServletRequest request, Integer page);
    PostResponseDTO.PostListDTO getSuggestPostList(Integer page, String address);
    PostResponseDTO.PostListDTO getHotPostList(Integer page);
    PostResponseDTO.PostListDTO getUserPostListByUserId(HttpServletRequest request, Integer page);
}
