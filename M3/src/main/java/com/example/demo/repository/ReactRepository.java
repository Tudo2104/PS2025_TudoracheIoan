package com.example.demo.repository;

import com.example.demo.entity.React;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ReactRepository extends JpaRepository<React, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    Optional<React> findById(Long id);
    @Query("SELECT r.type, COUNT(r) FROM React r WHERE r.postId = :postId GROUP BY r.type")
    List<Object[]> countReactionsByTypeForPost(@Param("postId") Long postId);

    @Query("SELECT r.type, COUNT(r) FROM React r WHERE r.commentId = :commentId GROUP BY r.type")
    List<Object[]> countReactionsByTypeForComment(@Param("commentId") Long commentId);

    void deleteByPostId(Long postId);

    void deleteByCommentId(Long commentId);


}
