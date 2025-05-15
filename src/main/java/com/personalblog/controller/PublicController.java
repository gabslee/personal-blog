package com.personalblog.controller;

import com.personalblog.domain.Post;
import com.personalblog.domain.User;
import com.personalblog.service.PostService;
import com.personalblog.service.UserService;
import com.personalblog.util.PostUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
public class PublicController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostUtils postUtils;

    @GetMapping("/")
    public String home(Model model, @RequestParam(defaultValue = "0") int page) {
        try {
            Page<Post> posts = postService.findByPublishedTrue(PageRequest.of(page, 12));
            
            // Criar um mapa para armazenar os resumos dos posts
            Map<UUID, String> postPreviews = new HashMap<>();
            
            // Processar cada post para criar um resumo limpo
            for (Post post : posts.getContent()) {
                if (post.getContent() != null) {
                    String preview = postUtils.stripHtml(post.getContent(), 150);
                    postPreviews.put(post.getId(), preview);
                }
            }
            
            // Adicionar os posts e seus resumos ao modelo
            model.addAttribute("posts", posts);
            model.addAttribute("postPreviews", postPreviews);
            
            return "public/home";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading posts: " + e.getMessage());
            return "public/home";
        }
    }

    @GetMapping("/posts/{slug}")
    public String viewPost(@PathVariable String slug, Model model, 
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request) {
        try {
            Optional<Post> postOptional = postService.findBySlugAndPublishedTrue(slug);
            
            if (postOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Post not found.");
                return "redirect:/";
            }
            
            Post post = postOptional.get();
            
            // Ensure all required fields have values to prevent template errors
            if (post.getPublishedAt() == null) {
                post.setPublishedAt(post.getCreatedAt() != null ? post.getCreatedAt() : LocalDateTime.now());
            }
            
            if (post.getCreatedAt() == null) {
                post.setCreatedAt(LocalDateTime.now());
            }
            
            // Sanitize content if needed
            String content = post.getContent();
            if (content != null) {
                // Fix potential unclosed HTML tags or other issues
                content = sanitizeHtml(content);
                post.setContent(content);
            } else {
                post.setContent(""); // Set empty content rather than null
            }
            
            // Generate base URL for sharing
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            model.addAttribute("baseUrl", baseUrl);
            
            model.addAttribute("post", post);
            return "public/post";
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error displaying post with slug: " + slug);
            e.printStackTrace();
            
            // Add error message and redirect to home
            redirectAttributes.addFlashAttribute("errorMessage", "The requested post could not be displayed: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Basic HTML sanitization to fix common issues that might cause template parsing errors
     */
    private String sanitizeHtml(String html) {
        if (html == null) return "";
        
        // Fix specific patterns that might cause issues
        
        // 1. Replace potentially problematic expressions that might conflict with Thymeleaf
        String sanitized = html.replace("${", "&#36;{")
                              .replace("#{", "&#35;{")
                              .replace("*{", "&#42;{")
                              .replace("@{", "&#64;{")
                              .replace("~{", "&#126;{");
        
        // 2. Fix unclosed iframe tags (a common issue)
        sanitized = Pattern.compile("<iframe([^>]*)>(?!</iframe>)").matcher(sanitized)
                         .replaceAll("<iframe$1></iframe>");
                         
        return sanitized;
    }

    @GetMapping("/about")
    public String about(Model model) {
        try {
            // Assuming we get the first user in the system as the blog owner
            User author = userService.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No author found"));
            
            model.addAttribute("author", author);
            return "public/about";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading author profile: " + e.getMessage());
            return "public/about";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
} 