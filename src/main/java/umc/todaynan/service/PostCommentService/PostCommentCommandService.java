package umc.todaynan.service.PostCommentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.apiPayload.exception.PostNotFoundException;
import umc.todaynan.apiPayload.exception.handler.PostCommentHandler;
import umc.todaynan.apiPayload.exception.handler.PostHandler;
import umc.todaynan.apiPayload.exception.handler.UserHandler;
import umc.todaynan.converter.PostCommentConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.PostCommentLikeRepository;
import umc.todaynan.repository.PostCommentRepository;
import umc.todaynan.repository.PostRepository;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.utils.ParseHeader;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommentCommandService implements PostCommentCommandServiceImpl {
    private final PostCommentRepository postCommentRepository;
    private final PostCommentLikeRepository postCommentLikeRepository;
    private final PostCommentConverter postCommentConverter;
    private final ParseHeader parseHeader;

    private final TokenService tokenService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    /*
    * 댓글 생성 API
    * 1. User 확인
    * 2. Request to DTO
    * 3. PostComment에 User, Post 세팅
    * 4. PostComment 저장
    * */
    @Override
    public PostResponseDTO.CreatePostCommentResultDTO createComment(Long post_id, Long comment_id, PostRequestDTO.CreatePostCommentDTO request, HttpServletRequest httpServletRequest) {
        User user = parseHeader.parseHeaderToUser(httpServletRequest);
        Post post = postRepository.findById(post_id).orElseThrow(() -> new PostNotFoundException("post not found"));;
        PostComment postComment;
        Integer maxBundleId = postCommentRepository.findMaxBundleId().orElse(null);
        if (comment_id ==null) { //최초댓글
            postComment = postCommentConverter.toPostComment(request, maxBundleId, post, user);
        }else {
            Optional<PostComment> parentPostComment = postCommentRepository.findById(comment_id);
            if(parentPostComment.isPresent()) {
                postComment = postCommentConverter.toPostChildComment(request, post, user, parentPostComment.get());
            }else {
                throw new  PostNotFoundException("post not found");
            }
        }
        postCommentRepository.save(postComment);
        return postCommentConverter.toCreateResultDTO(postComment);
//        return null;
    }

    /*
    * 댓글 수정 API
    * 1. User 확인
    * 2. 기존 PostComment에 새로운 데이터 저장
    * 3. PostComment 저장
    * */
    @Override
    public PostResponseDTO.UpdatePostCommentResultDTO updateComment(Long post_id, Long comment_id, PostRequestDTO.UpdatePostCommentDTO request, HttpServletRequest httpServletRequest) {
        Long userId = parseHeader.parseHeaderToUserId(httpServletRequest);
        if(postCommentRepository.updatePostCommentByUserIdAndPostId(userId, comment_id, request.getComment()) == 0)
            throw new GeneralException(ErrorStatus.POST_COMMENT_NOT_EXIST);
        return postCommentConverter.toUpdateResultDTO(userId, comment_id, request.getComment());
    }

    /*
     * 댓글 삭제 API
     * 1. User 확인
     * 2. 해당 Post에서 User가 쓴 댓글 삭제
     * */
    @Override
    public String deleteComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest) {
        postCommentRepository.deleteById(comment_id);
        return "삭제 성공";
    }

    /*
    * 댓글 좋아요 API
    * 1. User 확인
    * 2. 해당 Post에 User가 좋아요 누른 댓글 확인
    * 3. 좋아요 누르지 않았다면 좋아요 누르기
    * */
    @Override
    public PostResponseDTO.LikePostCommentResultDTO likeComment(Long post_id, Long comment_id, HttpServletRequest httpServletRequest) {
        User user = findUser(httpServletRequest);
        Optional<PostCommentLike> byUserIdAndPostCommentId = postCommentLikeRepository.findByUserIdAndPostCommentId(user.getId(), comment_id);
        if(!byUserIdAndPostCommentId.isPresent()) {
            PostComment postComment = postCommentRepository.findById(comment_id)
                    .orElseThrow(() -> new PostCommentHandler(ErrorStatus.POST_COMMENT_NOT_EXIST));
            PostCommentLike postCommentLike = toPostCommentLike(user, postComment);
            postCommentLikeRepository.save(postCommentLike);
            return postCommentConverter.toLikeResultDTO(postCommentLike);
        }
        return null;
    }
    //
    /*
    * 게시글 댓글 조회 API
    * 1. User 확인
    * 2. 해당 Post의 모든 PostComment 조회
    * */
    @Override
    public List<PostComment> getPostCommentList(Long post_id, HttpServletRequest httpServletRequest){
        List<PostComment> byPostId = postCommentRepository.findByPostId(post_id);

        return byPostId;
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

    private PostCommentLike toPostCommentLike(User user, PostComment postComment) {
        return PostCommentLike.builder()
                .user(user)
                .postComment(postComment)
                .build();
    }
}
