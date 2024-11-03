package umc.todaynan.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.PostCategory;
import umc.todaynan.repository.PostRepository;
import umc.todaynan.utils.ParseHeader;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService{

    private final PostRepository postRepository;
    private final ParseHeader parseHeader;
    private final PostConverter postConverter;


    @Override
    public PostResponseDTO.PostListDTO getEmployPostList(HttpServletRequest request, Integer page){
        User user = parseHeader.parseHeaderToUser(request);
        String middleAddress = getMiddleAddress(user.getAddress());
        return postConverter.toPostListDTO(
                postRepository.findAllByCategoryAndUser_AddressContainingOrderByCreatedAtDesc(PostCategory.EMPLOY, middleAddress, PageRequest.of(page, 10))
        );
    }

    @Override
    public PostResponseDTO.PostListDTO getChatPostList(HttpServletRequest request, Integer page) {
        User user = parseHeader.parseHeaderToUser(request);
        String middleAddress = getMiddleAddress(user.getAddress());
        return postConverter.toPostListDTO(
                postRepository.findAllByCategoryAndUser_AddressContainingOrderByCreatedAtDesc(PostCategory.CHAT, middleAddress, PageRequest.of(page, 10))
        );
    }

    @Override
    public PostResponseDTO.PostListDTO getSuggestPostList(Integer page, String address) {
        String middleAddress = "";
        if(address.equals("전체")){
            middleAddress = "전체";
        } else {
            String[] parts = address.split(" ");
            middleAddress = parts[1];
        }
        if(middleAddress.equals("전체")){
            return postConverter.toPostListDTO(
                    postRepository.findAllByCategoryOrderByCreatedAtDesc(PostCategory.SUGGEST, PageRequest.of(page, 10)));
        } else{
            return postConverter.toPostListDTO(
                    postRepository.findAllByCategoryAndUser_AddressContainingOrderByCreatedAtDesc(PostCategory.SUGGEST, middleAddress, PageRequest.of(page, 10)));

        }
    }

    @Override
    public PostResponseDTO.PostListDTO getHotPostList(Integer page) {
        return postConverter.toPostListDTO(
                postRepository.findAllByOrderByPostLikeListDesc(PageRequest.of(page, 10)));
    }

    @Override
    public PostResponseDTO.PostListDTO getUserPostListByUserId(HttpServletRequest request, Integer page) {
        User user = parseHeader.parseHeaderToUser(request);
        return postConverter.toPostListDTO(postRepository.findAllByUserId(user.getId(), PageRequest.of(page, 10)));
    }

    public String getMiddleAddress(String address) {
        String[] parts = address.split(" ");
        return parts[1];
    }
}
