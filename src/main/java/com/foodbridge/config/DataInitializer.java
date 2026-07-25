package com.foodbridge.config;

import com.foodbridge.entity.User;
import com.foodbridge.entity.User.Role;
import com.foodbridge.entity.User.UserStatus;
import com.foodbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

import org.springframework.context.annotation.Profile;

@Component
@Profile("local")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed default Donor if not present
        if (userRepository.findByEmail("donor@foodbridge.com").isEmpty()) {
            User donor = User.builder()
                    .name("Demo Donor Restaurant")
                    .email("donor@foodbridge.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.DONOR)
                    .phone("+19876543210")
                    .city("Chennai")
                    .latitude(13.0827)
                    .longitude(80.2707)
                    .status(UserStatus.ACTIVE)
                    .trustScore(5.0)
                    .build();
            userRepository.save(donor);
            logger.info("Seeded default donor user: donor@foodbridge.com / password123");
        }

        // Seed default NGO if not present
        if (userRepository.findByEmail("ngo@foodbridge.com").isEmpty()) {
            User ngo = User.builder()
                    .name("Demo Hope Shelter NGO")
                    .email("ngo@foodbridge.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.NGO)
                    .phone("+19876543211")
                    .city("Chennai")
                    .latitude(13.0850)
                    .longitude(80.2750)
                    .status(UserStatus.ACTIVE)
                    .trustScore(5.0)
                    .build();
            userRepository.save(ngo);
            logger.info("Seeded default NGO user: ngo@foodbridge.com / password123");
        }

        // Seed default Admin if not present
        if (userRepository.findByEmail("admin@foodbridge.com").isEmpty()) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email("admin@foodbridge.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN)
                    .phone("+19876543212")
                    .city("Chennai")
                    .latitude(13.0827)
                    .longitude(80.2707)
                    .status(UserStatus.ACTIVE)
                    .trustScore(5.0)
                    .build();
            userRepository.save(admin);
            logger.info("Seeded default Admin user: admin@foodbridge.com / password123");
        }
    }
}
