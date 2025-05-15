package com.personalblog.controller;

import com.personalblog.domain.Post;
import com.personalblog.domain.User;
import com.personalblog.service.FileStorageService;
import com.personalblog.service.PostService;
import com.personalblog.service.UserService;
import com.personalblog.util.PostUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PostUtils postUtils;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Post> posts = postService.findAllOrderByCreatedAtDesc(PageRequest.of(page, 10));
        model.addAttribute("posts", posts);
        return "admin/dashboard";
    }

    @GetMapping("/posts/new")
    public String newPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "admin/post_form";
    }

    @PostMapping("/posts")
    public String createPost(@ModelAttribute Post post,
                           @RequestParam("coverImage") MultipartFile coverImage,
                           @AuthenticationPrincipal UserDetails userDetails) {
        // Buscar o usuário pelo nome de usuário
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (!coverImage.isEmpty()) {
            String fileName = fileStorageService.storeFile(coverImage);
            post.setCoverImageUrl("/uploads/" + fileName);
        }

        post.setAuthor(user);
        post.setSlug(postUtils.generateSlug(post.getTitle()));
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Definir a data de publicação para os posts publicados
        // Para posts em draft, mantém como null para podermos definir corretamente quando for publicado
        if (post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postService.save(post);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/posts/edit/{id}")
    public String editPostForm(@PathVariable UUID id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        return "admin/post_form";
    }

    @PostMapping("/posts/update/{id}")
    public String updatePost(@PathVariable UUID id,
                           @ModelAttribute Post post,
                           @RequestParam("coverImage") MultipartFile coverImage) {
        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!coverImage.isEmpty()) {
            String fileName = fileStorageService.storeFile(coverImage);
            existingPost.setCoverImageUrl("/uploads/" + fileName);
        }

        // Check if the title has changed and regenerate slug if needed
        if (!existingPost.getTitle().equals(post.getTitle())) {
            existingPost.setTitle(post.getTitle());
            existingPost.setSlug(postUtils.generateSlug(post.getTitle()));
        } else {
            existingPost.setTitle(post.getTitle());
        }

        existingPost.setContent(post.getContent());
        
        // Status de publicação anterior
        boolean wasPublished = existingPost.isPublished();
        existingPost.setPublished(post.isPublished());
        existingPost.setUpdatedAt(LocalDateTime.now());

        // Se está sendo publicado agora (e não era antes)
        if (post.isPublished() && !wasPublished) {
            existingPost.setPublishedAt(LocalDateTime.now());
        }
        // Se está sendo despublicado, mantemos a data de publicação para histórico
        // Isso permitirá reordenar corretamente se for publicado novamente

        postService.save(existingPost);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/about/edit")
    public String editAboutForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Buscar o usuário pelo nome de usuário
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("user", user);
            return "admin/about_form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading user profile: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/about/update")
    public String updateAbout(@ModelAttribute User user,
                            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                            Model model) {
        try {
            User existingUser = userService.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                String fileName = fileStorageService.storeFile(profilePhoto);
                existingUser.setProfilePhotoUrl("/uploads/" + fileName);
            }

            // Atualizar apenas os campos do formulário, preservando outros campos
            if (user.getFullName() != null) {
                existingUser.setFullName(user.getFullName());
            }
            
            if (user.getAboutMe() != null) {
                existingUser.setAboutMe(user.getAboutMe());
            }
            
            if (user.getBio() != null) {
                existingUser.setBio(user.getBio());
            }

            userService.save(existingUser);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            model.addAttribute("user", user);
            return "admin/about_form";
        }
    }

    @PostMapping("/posts/delete")
    public String deletePost(@RequestParam(value = "postId") UUID postId,
                            RedirectAttributes redirectAttributes) {
        try {
            if (postId == null) {
                throw new RuntimeException("No post ID provided for deletion");
            }
            
            Post post = postService.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
            
            // Log para depuração
            System.out.println("Deleting post: " + post.getTitle() + " with ID: " + postId);
            
            // Opcional: Excluir arquivos associados
            if (post.getCoverImageUrl() != null && !post.getCoverImageUrl().isEmpty() 
                && !post.getCoverImageUrl().startsWith("http")) {
                // Apenas para arquivos locais, não URLs externas
                // fileStorageService.deleteFile(post.getCoverImageUrl());
                System.out.println("Would delete file: " + post.getCoverImageUrl());
            }
            
            postService.deleteById(postId);
            redirectAttributes.addFlashAttribute("successMessage", "Post deleted successfully: " + post.getTitle());
        } catch (Exception e) {
            System.err.println("Error deleting post: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting post: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/posts/view-raw/{id}")
    public String viewRawPost(@PathVariable UUID id, Model model) {
        try {
            Post post = postService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            
            model.addAttribute("post", post);
            return "admin/post_raw_view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error viewing post: " + e.getMessage());
            return "admin/dashboard";
        }
    }
} 