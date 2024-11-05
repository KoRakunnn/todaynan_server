package umc.todaynan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.converter.PostCommentConverter;
import umc.todaynan.converter.PostConverter;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.service.PostCommentService.PostCommentCommandService;
import umc.todaynan.service.PostService.PostService;
import umc.todaynan.service.PostService.PostServiceImpl;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

@Validated
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostRestController {
    private final PostService postService;
    private final PostCommentCommandService postCommentCommandService;
    private final PostConverter postConverter;
    @GetMapping("/employ")
    @Operation(summary = "구인 게시판 전체 검색",description = "구인 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page를 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> employPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ) {
        return ApiResponse.onSuccess(postService.getEmployPostList(request, page));
    }

    @GetMapping("/chat")
    @Operation(summary = "잡담 게시판 전체 검색",description = "잡담 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page를 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> chatPostList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ){
        return ApiResponse.onSuccess(postService.getChatPostList(request, page));
    }

    @GetMapping("/suggest")
    @Operation(summary = "추천 게시판 전체 검색",description = "추천 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page와 검색 지역을 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> suggestPostList(
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "검색 지역, default: 전체")
            @RequestParam(defaultValue = "전체") String address
    ){
        return ApiResponse.onSuccess(postService.getSuggestPostList(page, address));
    }

    @GetMapping("/hot")
    @Operation(summary = "HOT 게시판 전체 검색",description = "HOT 게시판 게시들의 목록을 조회하는 API이며, 페이징을 포함합니다. query String 으로 page을 주세요")
    public ApiResponse<PostResponseDTO.PostListDTO> hotPostList(
            @Parameter(description = "페이지 번호(1부터 시작), default: 1 / size = 10")
            @RequestParam(defaultValue = "1") Integer page
    ){
        return ApiResponse.onSuccess(postService.getHotPostList(page));
    }
    //
    @Operation(summary = "게시글 작성 API",description = "게시판에 유저가 게시글을 작성하는 API입니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "POST2005",description = "OK, 성공"),
    })
    @PostMapping("")
    public ApiResponse<PostResponseDTO.CreatePostResultDTO> createPost(@RequestBody PostRequestDTO.CreatePostDTO request,
                                                                   HttpServletRequest httpServletRequest){
        return ApiResponse.of(SuccessStatus.POST_CREATED, postService.createPost(request, httpServletRequest));
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
        return ApiResponse.of(SuccessStatus.POST_UPDATED, postService.updatePost(post_id, request, httpServletRequest));

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
        return ApiResponse.of(SuccessStatus.POST_DELETED, postService.deletePost(post_id, httpServletRequest));
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
                                                               HttpServletRequest httpServletRequest){
        return ApiResponse.of(SuccessStatus.POST_LIKE_SUCCESS, postService.likePost(post_id, httpServletRequest));
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
        return ApiResponse.of(SuccessStatus.POST_COMMENT_CREATED, postCommentCommandService.createComment(post_id, comment_id, request, httpServletRequest));
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
        return ApiResponse.of(SuccessStatus.POST_COMMENT_UPDATED, postCommentCommandService.updateComment(post_id, comment_id, request, httpServletRequest));
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
            return ApiResponse.of(SuccessStatus.POST_COMMENT_DELETED, postCommentCommandService.deleteComment(post_id, comment_id, httpServletRequest));
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
        return ApiResponse.of(SuccessStatus.POST_COMMENT_LIKE_SUCCESS, postCommentCommandService.likeComment(post_id, comment_id, httpServletRequest));
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
        return ApiResponse.of(SuccessStatus.POST_DETAIL_SUCCESS, postService.getPostDetail(post_id, httpServletRequest));
    }

}


