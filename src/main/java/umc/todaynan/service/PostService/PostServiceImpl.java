package umc.todaynan.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.PostNotFoundException;
import umc.todaynan.apiPayload.exception.handler.PostHandler;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.PostCategory;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.*;
import umc.todaynan.utils.ParseHeader;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    private final PostConverter postConverter;
    private final ParseHeader parseHeader;

    /*
     * 게시글 생성 API
     * 1. User 확인
     * 2. Request to DTO
     * 3. Post에 User 세팅4
     * 4. Post 저장
     * */
    @Transactional
    @Override
    public PostResponseDTO.CreatePostResultDTO createPost(PostRequestDTO.CreatePostDTO request, HttpServletRequest httpServletRequest) {
        User user = parseHeader.parseHeaderToUser(httpServletRequest);
        Post post = postConverter.toPost(request, user);
        postRepository.save(post);
        return postConverter.toCreateResultDTO(post);
    }

    /*
     * 게시글 수정 API
     * 1. User 확인
     * 2. 기존 Post에 새로운 데이터 저장
     * 3. Post 저장
     * */
    @Override
    public PostResponseDTO.UpdatePostResultDTO updatePost(Long post_id, PostRequestDTO.UpdatePostDTO request, HttpServletRequest httpServletRequest){
        Long userId = parseHeader.parseHeaderToUserId(httpServletRequest);
        if(postRepository.updatePostByUserId(post_id, userId, request.getTitle(), request.getContent())==0){

        }
        return postConverter.toUpdateResultDTO(post_id, request.getTitle(), request.getContent());
    }

    /*
    * 게시글 삭제 API
    * 1. User 확인
    * 2. User가 쓴 Post 삭제
    * */
    @Override
    public String deletePost(Long post_id, HttpServletRequest httpServletRequest){
        postRepository.deleteById(post_id); // post 삭제
        return "삭제 완료";
    }

    /*
    * 게시글 좋아요 API
    * 1. User 확인
    * 2. User가 쓴 Post 확인
    * 3. PostLike에 user_id, post_id 저장
    * */
    @Override
    public PostResponseDTO.LikePostResultDTO likePost(Long post_id, HttpServletRequest httpServletRequest){
        User user = parseHeader.parseHeaderToUser(httpServletRequest);
        Post post = postRepository.findPostById(post_id);
        Optional<PostLike> byUserAndPost = postLikeRepository.findByUserAndPost(user, post);
        if(!byUserAndPost.isPresent()){
            log.info("post like not exist");
            PostLike postLike = postConverter.toPostLike(user, post);
            return postConverter.toLikeResultDTO(postLikeRepository.save(postLike));
        }
        return null;
    }

    /*
     * 게시글 세부사항 조회 API
     * 1. User 확인
     * 2. User가 쓴 Post 확인
     * 3. Post 세부정보 조회 -> 페이징 사용하지 않고 리스트 형태로 PostComment 반환
     * */
    @Override
    public PostResponseDTO.PostDetailResultDTO getPostDetail(Long post_id, HttpServletRequest httpServletRequest){
        Post post = postRepository.findById(post_id).orElseThrow(() -> new PostNotFoundException("post not found"));
        Integer post_like_cnt = post.getPostLikeList().size();;

        return postConverter.toPostDetailResultDTO(post, post_like_cnt.longValue());
    }

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
        Long userId = parseHeader.parseHeaderToUserId(request);
        return postConverter.toPostListDTO(postRepository.findAllByUserId(userId, PageRequest.of(page, 10)));
    }

    public String getMiddleAddress(String address) {
        String[] parts = address.split(" ");
        return parts[1];
    }

    public Post findPost(Long post_id, User user){
        Post post = postRepository.findByIdAndUserId(post_id, user.getId())
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_EXIST));
        return post;
    }
}
