package umc.todaynan.web.dto.PostDTO;

import lombok.*;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.domain.enums.PostCategory;

import java.util.List;

public class PostResponseDTO {

    //글 작성, 삭제, 업데이트, 좋아요
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePostResultDTO{ //글 작성
        private Long post_id;
        private Long user_id;
        private String title;
        private String content;
        private PostCategory category;

//       private String fieldTest = "test";
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePostResultDTO{
        private Long post_id;
        private String title;
        private String content;
//        private String fieldTest = "test";
    }

    //게시판 조회(페이징)
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostListDTO {
        List<PostDTO> postList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDTO{
        Long postId;
        Long userId;
        String userNickname;
        String userAddress;
        String postTitle;
        String postContent;
        Integer postLike;
        Integer postComment;
        String createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikePostResultDTO {
        private Long post_like_id;
        private Long post_id;
        private Long user_id;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDetailResultDTO {
        private Long post_id;
        private String nick_name;
        private MyPet myPet;
        private String title;
        private String content;
        private Long post_like_cnt;
        private List<PostDetailCommentResultDTO> postCommentList;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDetailCommentResultDTO {
        private Long comment_id;
        private String nick_name;
        private MyPet myPet;
        private String title;
        private String content;
        private Long comment_like_cnt;
        private List<PostDetailCommentResultDTO> postChildList;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatePostCommentResultDTO{
        private Long post_comment_id;
        private Long post_id;
        private Long user_id;
        private String comment;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePostCommentResultDTO{
        private Long post_comment_id;
        private Long post_id;
        private Long user_id;
        private String comment;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LikePostCommentResultDTO{
        private Long post_comment_like_id;
        private Long post_comment_id;
        private Long user_id;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyPostCommentListDTO {
        List<PostCommentDTO> postCommentList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCommentDTO{
        Long postId;
        Long userId;
        String content;
        Integer commentLike;
        String createdAt;
    }

}

