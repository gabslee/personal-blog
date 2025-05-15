package com.personalblog.repository;

import com.personalblog.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByPublishedTrueOrderByPublishedAtDesc(Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Post> findBySlugAndPublishedTrue(String slug);
    Page<Post> findByAuthorId(UUID authorId, Pageable pageable);
    boolean existsBySlug(String slug);
    Optional<Post> findBySlug(String slug);
} 