package umc.todaynan.repository.QueryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.domain.entity.User.User.QUser;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserPrefer.PreferCategory;
import umc.todaynan.domain.entity.User.UserPrefer.UserPrefer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static umc.todaynan.domain.entity.User.User.QUser.user;
import static umc.todaynan.domain.entity.User.UserPrefer.QPreferCategory.preferCategory;
import static umc.todaynan.domain.entity.User.UserPrefer.QUserPrefer.userPrefer;

@Slf4j
@Repository
public class UserPreferQueryDslRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public UserPreferQueryDslRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Modifying
    @Transactional
    public void changePreferList(long userId, List<Integer> interests) {
        query.delete(userPrefer)
                .where(userPrefer.user.id.eq(userId))
                .execute();
        log.info("[Repository - UserPreferQueryDslRepository] {}의 관심사 모두 제거", userId);

        User userEntity = query.select(user)
                .from(user)
                .where(user.id.eq(userId))
                .fetchOne();

        Map<Long, PreferCategory> categoryMap = query.selectFrom(preferCategory)
                .where(preferCategory.id.in(interests.stream().map(Long::valueOf).collect(Collectors.toList())))
                .fetch()
                .stream()
                .collect(Collectors.toMap(PreferCategory::getId, Function.identity()));

        List<UserPrefer> newUserPrefers = interests.stream()
                .map(interest -> UserPrefer.builder()
                        .preferCategory(categoryMap.get(Long.valueOf(interest)))
                        .user(userEntity)
                        .build())
                .toList();

        newUserPrefers.forEach(em::persist);
    }

}
