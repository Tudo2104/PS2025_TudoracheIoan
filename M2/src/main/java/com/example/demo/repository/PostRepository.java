package com.example.demo.repository;

import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface PostRepository extends JpaRepository<Post, Long> {

    Post findPostById(Long id);
    List<Post> findAllByOrderByCreatedAtDesc();
    Optional<Post> findByIdAndUserId(Long id, Long userId);
    List<Post> findAllByHashtagsIn(Set<Hashtag> hashtags);

    List<Post> findAllByContentContaining(String keyword);
    List<Post> findAllByUserId(Long userId);

}