package umc.todaynan.domain.entity.User.UserPrefer;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import umc.todaynan.domain.common.DateBaseEntity;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.service.UserService.UserService;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPrefer extends DateBaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PreferCategory preferCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
