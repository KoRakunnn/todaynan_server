package umc.todaynan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.converter.PostCommentConverter;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.PostCommentService.PostCommentCommandService;
import umc.todaynan.service.PostService.PostCommandService;
import umc.todaynan.service.PostService.PostQueryService;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostRestController {
    private final PostCommandService postCommandService;
    private final PostCommentCommandService postCommentCommandService;
    private final PostQueryService postQueryService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public String getMiddleAddress(User user) {
        String address = user.getAddress();
        String[] parts = address.split(" ");
        return parts[1];
    }

    @GetMapping("/employ")
    @Operation(summary = "구인 게시판 전체 검색",description = "구인 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page를 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> employPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ){
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            String middleAddress = getMiddleAddress(user.get());

            Page<Post> employPostPage = postQueryService.getEmployPostList(page-1, middleAddress);
            return ApiResponse.onSuccess(PostConverter.toPostListDTO(employPostPage));
        }else {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }
    }

    @GetMapping("/chat")
    @Operation(summary = "잡담 게시판 전체 검색",description = "잡담 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page를 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> chatPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ){
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            String middleAddress = getMiddleAddress(user.get());

            Page<Post> chatPostPage = postQueryService.getChatPostList(page-1, middleAddress);
            return ApiResponse.onSuccess(PostConverter.toPostListDTO(chatPostPage));
        }else {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }
    }

    @GetMapping("/suggest")
    @Operation(summary = "추천 게시판 전체 검색",description = "추천 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page와 검색 지역을 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> suggestPostList(
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "검색 지역, default: 전체")
            @RequestParam(defaultValue = "전체") String address
    ){
        String middleAddress = "";
        if(address.equals("전체")){
            middleAddress = "전체";
        } else {
            String[] parts = address.split(" ");
            middleAddress = parts[1];
        }

        Page<Post> suggestPostPage = postQueryService.getSuggestPostList(page-1, middleAddress);
        return ApiResponse.onSuccess(PostConverter.toPostListDTO(suggestPostPage));
    }

    @GetMapping("/hot")
    @Operation(summary = "HOT 게시판 전체 검색",description = "HOT 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page을 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> hotPostList(
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ){

        Page<Post> hotPostPage = postQueryService.getHotPostList(page-1);
        return ApiResponse.onSuccess(PostConverter.toPostListDTO(hotPostPage));
    }
    //
    @Operation(summary = "게시글 작성 API",description = "게시판에 유저가 게시글을 작성하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2005",description = "OK, 성공"),
    })
    @PostMapping("")
    public ApiResponse<PostResponseDTO.CreatePostResultDTO> createPost(@RequestBody PostRequestDTO.CreatePostDTO request,
                                                                   HttpServletRequest httpServletRequest){
        Post post = postCommandService.createPost(request, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_CREATED, PostConverter.toCreateResultDTO(post));
    }

    @Operation(summary = "게시글 수정 API",description = "유저가 작성한 게시글을 수정하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2006",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다")
    })
    @PatchMapping("/{post_id}")
    public ApiResponse<PostResponseDTO.UpdatePostResultDTO> updatePost(@PathVariable("post_id") Long post_id,
                                                                   @RequestBody PostRequestDTO.UpdatePostDTO request,
                                                                   HttpServletRequest httpServletRequest){
        Post post = postCommandService.updatePost(post_id, request, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_UPDATED, PostConverter.toUpdateResultDTO(post));

    }

    @Operation(summary = "게시글 삭제 API",description = "게시판에 유저가 게시글을 삭제하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2007",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다")
    })
    @DeleteMapping("/{post_id}")
    public ApiResponse<String> deletePost(@PathVariable("post_id") Long post_id,
                                          HttpServletRequest httpServletRequest){
        Boolean isSuccess = postCommandService.deletePost(post_id, httpServletRequest);
        if(isSuccess){
            return ApiResponse.of(SuccessStatus.POST_DELETED, "게시글이 성공적으로 삭제되었습니다");
        }
        return null;
    }

    @Operation(summary = "게시글 좋아요 API",description = "게시판에 유저가 게시글을 작성하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2008",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다")
    })
    @PostMapping("/like/{post_id}")
    public ApiResponse<PostResponseDTO.LikePostResultDTO> likePost(@PathVariable("post_id") Long post_id,
//                                                               @RequestBody PostRequestDTO.LikeDTO request,
                                                               HttpServletRequest httpServletRequest){
        PostLike postLike = postCommandService.likePost(post_id, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_LIKE_SUCCESS, PostConverter.toLikeResultDTO(postLike));
    }

    @Operation(summary = "댓글 작성 API",description = "유저가 댓글을 작성하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMENT2004",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다")
    })
    @PostMapping("/comment/{post_id}")
    public ApiResponse<PostResponseDTO.CreatePostCommentResultDTO> createPostComment(@PathVariable("post_id") Long post_id,
                                                                                     @RequestParam(name = "comment_id", required = false) Long comment_id,
                                                                                 @RequestBody PostRequestDTO.CreatePostCommentDTO request,
                                                                                 HttpServletRequest httpServletRequest){
        PostComment postComment = postCommentCommandService.createComment(post_id, comment_id, request, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_COMMENT_CREATED, PostCommentConverter.toCreateResultDTO(postComment));
    }

    @Operation(summary = "댓글 수정 API",description = "유저가 작성한 댓글을 수정하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMENT2005",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다"),
            @Parameter(name = "comment_id", description = "댓글의 id, path variable 입니다")
    })
    @PatchMapping("/comment/{post_id}/{comment_id}")
    public ApiResponse<PostResponseDTO.UpdatePostCommentResultDTO> updatePostComment(@PathVariable("post_id") Long post_id,
                                                                                 @PathVariable("comment_id") Long comment_id,
                                                                                 @RequestBody PostRequestDTO.UpdatePostCommentDTO request,
                                                                                 HttpServletRequest httpServletRequest){
        PostComment postComment = postCommentCommandService.updateComment(post_id, comment_id, request, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_COMMENT_UPDATED, PostCommentConverter.toUpdateResultDTO(postComment));
    }

    @Operation(summary = "댓글 삭제 API",description = "유저가 댓글을 삭제하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMENT2006",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다"),
            @Parameter(name = "comment_id", description = "댓글의 id, path variable 입니다")
    })
    @DeleteMapping("/comment/{post_id}/{comment_id}")
    public ApiResponse<String> deletePostComment(@PathVariable("post_id") Long post_id,
                                                 @PathVariable("comment_id") Long comment_id,
                                                 HttpServletRequest httpServletRequest){
        Boolean isSuccess = postCommentCommandService.deleteComment(post_id, comment_id, httpServletRequest);
        if(isSuccess){
            return ApiResponse.of(SuccessStatus.POST_COMMENT_DELETED, "댓글이 성공적으로 삭제되었습니다");
        }
        return null;
    }

    @Operation(summary = "댓글 좋아요 API",description = "유저가 댓글에 좋아요를 누르는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMENT2007",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다"),
            @Parameter(name = "comment_id", description = "댓글의 id, path variable 입니다")
    })
    @PostMapping("/comment/like/{post_id}/{comment_id}")
    public ApiResponse<PostResponseDTO.LikePostCommentResultDTO> likePostComment(@PathVariable("post_id") Long post_id,
                                                                             @PathVariable("comment_id") Long comment_id,
                                                                             HttpServletRequest httpServletRequest){
        PostCommentLike postCommentLike = postCommentCommandService.likeComment(post_id, comment_id, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_COMMENT_LIKE_SUCCESS, PostCommentConverter.toLikeResultDTO(postCommentLike));
    }

    @Operation(summary = "게시글 세부사항 조회 API",description = "게시글 클릭시 상세정보를 보여주는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2009",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "post_id", description = "게시글의 id, path variable 입니다"),
    })
    @GetMapping("/detail/{post_id}")
    public ApiResponse<PostResponseDTO.PostDetailResultDTO> PostDetail(@PathVariable("post_id") Long post_id,
                                                               HttpServletRequest httpServletRequest){
        PostResponseDTO.PostDetailResultDTO postDetail = postCommandService.getPostDetail(post_id, httpServletRequest);
        return ApiResponse.of(SuccessStatus.POST_DETAIL_SUCCESS, postDetail);
    }

}


