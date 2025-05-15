package com.personalblog.util;

import com.personalblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class PostUtils {
    
    @Autowired
    private PostRepository postRepository;
    
    public String generateSlug(String title) {
        if (!StringUtils.hasText(title)) {
            return "";
        }
        
        // Convert to lowercase and replace non-alphanumeric characters with hyphens
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        
        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        
        // Check if slug already exists and add a suffix if needed
        return generateUniqueSlug(slug);
    }
    
    private String generateUniqueSlug(String baseSlug) {
        String uniqueSlug = baseSlug;
        int counter = 1;
        
        // Keep checking until we find a unique slug
        while (postRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        
        return uniqueSlug;
    }
    
    public String generatePreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        
        // Remove HTML tags
        String plainText = content.replaceAll("<[^>]*>", "");
        
        // Limit to 200 characters
        if (plainText.length() > 200) {
            return plainText.substring(0, 197) + "...";
        }
        
        return plainText;
    }
    
    /**
     * Remove todas as tags HTML do texto e retorna texto puro
     * @param html O conteúdo HTML para limpar
     * @param maxLength O tamanho máximo do texto (0 para sem limite)
     * @return Texto limpo sem tags HTML, possivelmente abreviado
     */
    public String stripHtml(String html, int maxLength) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        // Remover tags HTML (método simples e seguro)
        String plainText = html.replaceAll("<[^>]+>", "")
                              .replaceAll("&nbsp;", " ")
                              .replaceAll("&amp;", "&")
                              .replaceAll("&lt;", "<")
                              .replaceAll("&gt;", ">")
                              .replaceAll("&quot;", "\"")
                              .replaceAll("&#39;", "'");
                              
        // Remover espaços extras
        plainText = plainText.replaceAll("\\s+", " ").trim();
        
        // Abreviar se necessário
        if (maxLength > 0 && plainText.length() > maxLength) {
            return plainText.substring(0, maxLength - 3) + "...";
        }
        
        return plainText;
    }
} 