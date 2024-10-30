package umc.todaynan.domain.entity.User.User;

import jakarta.persistence.*;
import lombok.*;
import umc.todaynan.domain.common.DateBaseEntity;
import umc.todaynan.domain.entity.Chat.Chat;
import umc.todaynan.domain.entity.Chat.ChatRoom;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;
import umc.todaynan.domain.entity.Post.PostCommentLike.PostCommentLike;
import umc.todaynan.domain.entity.Post.PostLike.PostLike;
import umc.todaynan.domain.entity.User.UserBlocking.UserBlocking;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.domain.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends DateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 10)
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MyPet myPet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPrefer> userPreferList = new ArrayList<>();

    @OneToMany(mappedBy = "blockingUser", cascade = CascadeType.ALL)
    private List<UserBlocking> userBlockingList = new ArrayList<>();

    @OneToMany(mappedBy = "blockedUser", cascade = CascadeType.ALL)
    private List<UserBlocking> userBlockedList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Chat> chatList = new ArrayList<>();

    @OneToMany(mappedBy = "sendUser", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRoomSendList = new ArrayList<>();

    @OneToMany(mappedBy = "receiveUser", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRoomReceiveList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostLike> postLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostComment> postCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostCommentLike> postCommentLikeList = new ArrayList<>();

}
