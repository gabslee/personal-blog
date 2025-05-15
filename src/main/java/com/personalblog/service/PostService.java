package com.personalblog.service;

import com.personalblog.domain.Post;
import com.personalblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }
    
    public Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    public Page<Post> findByPublishedTrue(Pageable pageable) {
        return postRepository.findByPublishedTrueOrderByPublishedAtDesc(pageable);
    }
    
    public Optional<Post> findById(UUID id) {
        return postRepository.findById(id);
    }
    
    public Optional<Post> findBySlug(String slug) {
        return postRepository.findBySlug(slug);
    }
    
    public Optional<Post> findBySlugAndPublishedTrue(String slug) {
        return postRepository.findBySlugAndPublishedTrue(slug);
    }
    
    public Post save(Post post) {
        return postRepository.save(post);
    }
    
    public void deleteById(UUID id) {
        postRepository.deleteById(id);
    }
} 