package umc.todaynan.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

public interface PostQueryService {

    PostResponseDTO.PostListDTO getEmployPostList(HttpServletRequest request, Integer page);
    PostResponseDTO.PostListDTO getChatPostList(HttpServletRequest request, Integer page);
    PostResponseDTO.PostListDTO getSuggestPostList(Integer page, String address);
    PostResponseDTO.PostListDTO getHotPostList(Integer page);
    PostResponseDTO.PostListDTO getUserPostListByUserId(HttpServletRequest request, Integer page);
}
