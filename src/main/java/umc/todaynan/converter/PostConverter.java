package umc.todaynan.converter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.web.dto.PostDTO.PostRequestDTO;
import umc.todaynan.web.dto.PostDTO.PostResponseDTO;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostConverter {

    public Post toPost(PostRequestDTO.CreatePostDTO request, User user) {
        return Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .user(user)
                .build();
    }

    public PostResponseDTO.CreatePostResultDTO toCreateResultDTO(Post post) {
        return PostResponseDTO.CreatePostResultDTO.builder()
                .post_id(post.getId())
                .user_id(post.getUser().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .build();
    }

    public PostResponseDTO.PostDTO toPostDTO(Post post) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String formattedCreatedAt = post.getCreatedAt().format(formatter);

        return PostResponseDTO.PostDTO.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .userNickname(post.getUser().getNickName())
                .userAddress(post.getUser().getAddress())
                .postTitle(post.getTitle())
                .postContent(post.getContent())
                .postLike(post.getPostLikeList().size())
                .postComment(post.getPostCommentList().size())
                .createdAt(formattedCreatedAt)
                .build();
    }

    public PostResponseDTO.PostListDTO toPostListDTO(Page<Post> postList) {
        List<PostResponseDTO.PostDTO> postDTOList = postList.stream()
                .map(this::toPostDTO).collect(Collectors.toList());

        return PostResponseDTO.PostListDTO.builder()
                .isLast(postList.isLast())
                .isFirst(postList.isFirst())
                .totalPage(postList.getTotalPages())
                .totalElements(postList.getTotalElements())
                .listSize(postDTOList.size())
                .postList(postDTOList)
                .build();
    }

    public PostResponseDTO.UpdatePostResultDTO toUpdateResultDTO(Long postId, String title, String content) {
        return PostResponseDTO.UpdatePostResultDTO.builder()
                .post_id(postId)
                .title(title)
                .content(content)
                .build();
    }


    public PostResponseDTO.LikePostResultDTO toLikeResultDTO(PostLike postLike) {
        return PostResponseDTO.LikePostResultDTO.builder()
                .post_like_id(postLike.getId())
                .post_id(postLike.getPost().getId())
                .user_id(postLike.getUser().getId())
                .build();
    }

    public PostResponseDTO.PostDetailResultDTO toPostDetailResultDTO(Post post, Long post_cnt) {
        List<PostResponseDTO.PostDetailCommentResultDTO> postDetailCommentResultDTO
                = toPostDetailCommentResultDTO(post.getPostCommentList(), 0L);

        return PostResponseDTO.PostDetailResultDTO.builder()
                .post_id(post.getId())
                .nick_name(post.getUser().getNickName())
                .myPet(post.getUser().getMyPet())
                .title(post.getTitle())
                .content(post.getContent())
                .post_like_cnt(post_cnt)
                .postCommentList(postDetailCommentResultDTO)
                .build();
    }

    public List<PostResponseDTO.PostDetailCommentResultDTO> toPostDetailCommentResultDTO(List<PostComment> postCommentList, Long depth) {

        return postCommentList.stream()
                .filter(postComment -> postComment.getDepth().equals(depth))
                .map(postComment -> {
                    List<PostResponseDTO.PostDetailCommentResultDTO> childCommentsDTO =
                            postComment.getChildComments() != null
                                    ? toPostDetailCommentResultDTO(postComment.getChildComments(), depth + 1)
                                    : new ArrayList<>();

                    return PostResponseDTO.PostDetailCommentResultDTO.builder()
                            .comment_id(postComment.getId())
                            .postChildList(childCommentsDTO)
                            .comment_like_cnt((long) postComment.getPostCommentLikes().size())
                            .content(postComment.getComment())
                            .myPet(postComment.getUser().getMyPet())
                            .nick_name(postComment.getUser().getNickName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public PostLike toPostLike(User user, Post post) {
        return PostLike.builder()
                .user(user)
                .post(post)
                .build();
    }
}


