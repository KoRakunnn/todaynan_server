package umc.todaynan.domain.entity.Post.Post;

import jakarta.persistence.*;
import lombok.*;
import umc.todaynan.domain.common.DateBaseEntity;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;
import umc.todaynan.domain.enums.PostCategory;
import umc.todaynan.domain.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends DateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 20)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostLike> postLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostComment> postCommentList = new ArrayList<>();


}
