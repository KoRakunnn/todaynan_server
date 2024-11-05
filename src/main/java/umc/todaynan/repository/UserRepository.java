package umc.todaynan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.domain.entity.User.User.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<Long> findUserIdByEmail(@Param("email") String email);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickname);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.nickName = :nickName WHERE u.id = :userId")
    void updateNickNameById(@Param("userId") Long userId, @Param("nickName") String nickName);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.address = :address WHERE u.id = :userId")
    void updateAddressById(@Param("userId") Long userId, @Param("address") String address);


}
