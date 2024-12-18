package umc.todaynan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.domain.entity.Post.PostComment.PostComment;

import javax.swing.text.html.Option;
import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Optional<PostComment> findById(Long Id);
    List<PostComment> findByPostId(Long postId);
    @Query("SELECT pc FROM PostComment pc  WHERE pc.user.id = :userId")
    List<PostComment> findAllByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COALESCE(MAX(pc.bundleId), 0) FROM PostComment pc")
    Optional<Integer> findMaxBundleId();

    @Modifying
    @Transactional
    @Query("UPDATE PostComment pc SET pc.comment = :comment WHERE pc.user.id = :userId AND pc.id = :commentId")
    int updatePostCommentByUserIdAndPostId(@Param("userId") Long userId,
                                           @Param("commentId") Long commentId,
                                           @Param("comment") String comment);
}
