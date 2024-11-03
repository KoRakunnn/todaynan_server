package umc.todaynan.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.PostNotFoundException;
import umc.todaynan.apiPayload.exception.handler.PostHandler;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.*;
import umc.todaynan.service.PostCommentService.PostCommentCommandService;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommandService implements PostCommandServiceImpl{
    @Autowired
    private final PostRepository postRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PostLikeRepository postLikeRepository;
    @Autowired
    private final PostCommentCommandService postCommentCommandService;
    @Autowired
    private final TokenService tokenService;
    @Autowired
    private PostCommentLikeRepository postCommentLikeRepository;

    /*
     * 게시글 생성 API
     * 1. User 확인
     * 2. Request to DTO
     * 3. Post에 User 세팅4
     * 4. Post 저장
     * */
    @Transactional
    @Override
    public Post createPost(PostRequestDTO.CreatePostDTO request, HttpServletRequest httpServletRequest) {
        User user = findUser(httpServletRequest);
        Post post = PostConverter.toPost(request);
        post.setUser(user);
        return postRepository.save(post);
    }

    /*
     * 게시글 수정 API
     * 1. User 확인
     * 2. 기존 Post에 새로운 데이터 저장
     * 3. Post 저장
     * */
    @Override
    public Post updatePost(Long post_id, PostRequestDTO.UpdatePostDTO request, HttpServletRequest httpServletRequest){
        User user = findUser(httpServletRequest);
        Post post = findPost(post_id, user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        return postRepository.save(post);
    }

    /*
    * 게시글 삭제 API
    * 1. User 확인
    * 2. User가 쓴 Post 삭제
    * */
    @Override
    public Boolean deletePost(Long post_id, HttpServletRequest httpServletRequest){
        User user = findUser(httpServletRequest);
        Post post = findPost(post_id, user);
        postRepository.deleteById(post_id); // post 삭제
        return true;
    }

    /*
    * 게시글 좋아요 API
    * 1. User 확인
    * 2. User가 쓴 Post 확인
    * 3. PostLike에 user_id, post_id 저장
    * */
    @Override
    public PostLike likePost(Long post_id, HttpServletRequest httpServletRequest){
        User user = findUser(httpServletRequest);
        Post post = postRepository.findPostById(post_id);
        Optional<PostLike> byUserAndPost = postLikeRepository.findByUserAndPost(user, post);
        if(!byUserAndPost.isPresent()){
            log.info("post like not exist");
            PostLike postLike = toPostLike(user, post);
            return postLikeRepository.save(postLike);
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
        Integer post_like_cnt = post.getPostLikeList().size();
        List<PostComment> postCommentList = post.getPostCommentList();
        List<PostCommentListDTO> post_comments = postCommentList.stream()
                .map(postComment -> new PostCommentListDTO(
                        postComment.getId(),
                        postComment.getUser().getNickName(),
                        postComment.getUser().getMyPet(),
                        postComment.getComment(),
                        postComment.getPostCommentLikes().size(),
                        postComment.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                        postComment.getBundleId(),
                        postComment.getDepth(),
                        postComment.getParentComment() != null ? postComment.getParentComment().getId() : null)
                )
                .collect(Collectors.toList());

        PostResponseDTO.PostDetailResultDTO postDetailResultDTO = toPostDetailResultDTO(post, post_like_cnt.longValue(), post_comments);
        postDetailResultDTO.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        return postDetailResultDTO;
    }

    public User findUser(HttpServletRequest httpServletRequest){
        String email = tokenService.getUid(tokenService.getJwtFromHeader(httpServletRequest));
        User user = userRepository.findByEmail(email) //헤더 정보에서 추출한 이메일로 체크
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_EXIST));
        return user;
    }

    public Post findPost(Long post_id, User user){
        Post post = postRepository.findByIdAndUserId(post_id, user.getId())
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_EXIST));
        return post;
    }

    private PostLike toPostLike(User user, Post post) {
        return PostLike.builder()
                .user(user)
                .post(post)
                .build();
    }

    public static PostResponseDTO.PostDetailResultDTO toPostDetailResultDTO(Post post, Long post_cnt, List<PostCommentListDTO> post_comments) {
        return PostResponseDTO.PostDetailResultDTO.builder()
                .post_id(post.getId())
                .nick_name(post.getUser().getNickName())
                .myPet(post.getUser().getMyPet())
                .title(post.getTitle())
                .content(post.getContent())
                .post_like_cnt(post_cnt)
                .postCommentList(post_comments)
                .build();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostCommentListDTO{
        private Long post_comment_id;
        private String nick_name;
        private MyPet myPet;
        private String content;
        private Integer post_comment_like_cnt;
        private String createdAt;
        private Long bundleId;
        private Long depth;
        private Long parentId;
    }
}
