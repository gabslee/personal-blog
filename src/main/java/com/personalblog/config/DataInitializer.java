package com.personalblog.config;

import com.personalblog.domain.User;
import com.personalblog.domain.UserRole;
import com.personalblog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if admin user exists, if not create one
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin"));  // Default password
            adminUser.setEmail("admin@example.com");
            adminUser.setFullName("Admin User");
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setEnabled(true);
            adminUser.setAboutMe("This is the blog administrator. You can customize this section from the admin dashboard.");
            
            userRepository.save(adminUser);
            
            System.out.println("Admin user created with username: admin and password: admin");
        }
    }
} 