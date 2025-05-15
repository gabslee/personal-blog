package com.personalblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsPath = Paths.get(uploadDir);
        String uploadsAbsolutePath = uploadsPath.toFile().getAbsolutePath();
        
        // Mapear o caminho "/uploads/**" para o diretório físico
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsAbsolutePath + "/");
                
        // Manter os mapeamentos padrão
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 