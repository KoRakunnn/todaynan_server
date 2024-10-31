package umc.todaynan.domain.entity.Post.PostComment;

import jakarta.persistence.*;
import lombok.*;
import umc.todaynan.domain.common.DateBaseEntity;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.User.User.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_comment", indexes = @Index(name = "idx_bundle_id", columnList = "bundle_id"))
@AllArgsConstructor
public class PostComment extends DateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long depth;

    @Column(nullable = false)
    private Long bundleId;

    @Column(nullable = false, length = 200)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private PostComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostComment> childComments = new ArrayList<>();

    @OneToMany(mappedBy = "postComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostCommentLike> postCommentLikes = new ArrayList<>();
}
