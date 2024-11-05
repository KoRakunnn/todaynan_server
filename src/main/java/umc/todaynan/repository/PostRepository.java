package umc.todaynan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.domain.entity.Post.Post.Post;
import umc.todaynan.domain.enums.PostCategory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Post save(Post post); //게시글 저장
    Optional<Post> findByIdAndUserId(Long id, Long user_id); //해당 유저가 쓴 게시글이 있는지 확인
    Page<Post> findAllByCategoryOrderByCreatedAtDesc(PostCategory category, PageRequest pageRequest);
    Page<Post> findAllByCategoryAndUser_AddressContainingOrderByCreatedAtDesc(PostCategory category, String address, PageRequest pageRequest);
    Page<Post> findAllByOrderByPostLikeListDesc(PageRequest pageRequest);
    Page<Post> findAllByUserId(long userId, PageRequest pageRequest);
    List<Post> findAllById(Iterable<Long> postId);

    Post findPostById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.title = :title, p.content = :content WHERE p.id = :postId AND p.user.id = :userId")
    int updatePostByUserId(@Param("postId") Long postId, @Param("userId") Long userId,
                           @Param("title") String title, @Param("content") String content);
}
