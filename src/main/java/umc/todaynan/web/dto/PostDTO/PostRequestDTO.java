package umc.todaynan.web.dto.PostDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import umc.todaynan.domain.enums.PostCategory;


public class PostRequestDTO {
    @Builder
    @Getter
    public static class CreatePostDTO{
        @NotBlank
        String content;

        @NotBlank
        String title;

        @NotBlank
        PostCategory category;
    }

    @Getter
    public static class UpdatePostDTO{
        @NotBlank
        String content;

        @NotBlank
        String title;
    }

    @Getter
    public static class CreatePostCommentDTO{
        @NotBlank
        String comment;
    }

    @Getter
    public static class UpdatePostCommentDTO{
        @NotBlank
        String comment;
    }

    @Getter
    public static class DeleteDTO{
//        @NotNull
//        Long user_id;
    }

    @Getter
    public static class LikeDTO{
//        @NotNull
//        Long user_id;
    }
}
