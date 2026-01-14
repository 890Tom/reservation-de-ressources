package com.reservation.config;

import com.reservation.entity.Resource;
import com.reservation.entity.User;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import com.reservation.enums.Role;
import com.reservation.repository.ResourceRepository;
import com.reservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                     ResourceRepository resourceRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("========================================");
            logger.info("Vérification des données initiales...");
            logger.info("========================================");
            
            // ============================================
            // INITIALISATION DES UTILISATEURS
            // ============================================
            if (userRepository.count() == 0) {
                logger.info("Création des utilisateurs de test...");
                
                // 1. ADMIN
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@reservation.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setDepartment("IT");
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
                logger.info("ADMIN créé - Username: admin | Password: admin123");
                
                // 2. MANAGER
                User manager = new User();
                manager.setUsername("manager");
                manager.setEmail("manager@reservation.com");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setFirstName("Jean");
                manager.setLastName("Manager");
                manager.setDepartment("Operations");
                manager.setRole(Role.MANAGER);
                manager.setActive(true);
                userRepository.save(manager);
                logger.info("MANAGER créé - Username: manager | Password: manager123");
                
                // 3. USER 1
                User user1 = new User();
                user1.setUsername("user");
                user1.setEmail("user@reservation.com");
                user1.setPassword(passwordEncoder.encode("user123"));
                user1.setFirstName("Marie");
                user1.setLastName("Dupont");
                user1.setDepartment("Sales");
                user1.setRole(Role.USER);
                user1.setActive(true);
                userRepository.save(user1);
                logger.info("USER créé - Username: user | Password: user123");
                
                // 4. USER 2
                User user2 = new User();
                user2.setUsername("john");
                user2.setEmail("john@reservation.com");
                user2.setPassword(passwordEncoder.encode("john123"));
                user2.setFirstName("John");
                user2.setLastName("Smith");
                user2.setDepartment("Marketing");
                user2.setRole(Role.USER);
                user2.setActive(true);
                userRepository.save(user2);
                logger.info("USER créé - Username: john | Password: john123");
                
                logger.info("{} utilisateurs créés", userRepository.count());
            } else {
                logger.info("Des utilisateurs existent déjà ({} utilisateurs)", userRepository.count());
            }
            
            // ============================================
            // INITIALISATION DES RESSOURCES
            // ============================================
            if (resourceRepository.count() == 0) {
                logger.info("📝 Création des ressources de test...");
                
                // SALLES DE RÉUNION
                Resource salle1 = new Resource();
                salle1.setName("Salle de Réunion A");
                salle1.setDescription("Grande salle de réunion avec écran et vidéoprojecteur");
                salle1.setType(ResourceType.MEETING_ROOM);
                salle1.setStatus(ResourceStatus.AVAILABLE);
                salle1.setLocation("Bâtiment A - Étage 2");
                salle1.setCapacity(10);
                resourceRepository.save(salle1);
                
                Resource salle2 = new Resource();
                salle2.setName("Salle de Réunion B");
                salle2.setDescription("Petite salle pour réunions privées");
                salle2.setType(ResourceType.MEETING_ROOM);
                salle2.setStatus(ResourceStatus.AVAILABLE);
                salle2.setLocation("Bâtiment A - Étage 3");
                salle2.setCapacity(6);
                resourceRepository.save(salle2);
                
                Resource salle3 = new Resource();
                salle3.setName("Salle de Conférence");
                salle3.setDescription("Grande salle pour présentations et conférences");
                salle3.setType(ResourceType.MEETING_ROOM);
                salle3.setStatus(ResourceStatus.AVAILABLE);
                salle3.setLocation("Bâtiment B - Rez-de-chaussée");
                salle3.setCapacity(50);
                resourceRepository.save(salle3);
                
                // ÉQUIPEMENTS
                Resource laptop1 = new Resource();
                laptop1.setName("Laptop Dell XPS 15");
                laptop1.setDescription("Ordinateur portable haute performance");
                laptop1.setType(ResourceType.EQUIPMENT);
                laptop1.setStatus(ResourceStatus.AVAILABLE);
                laptop1.setLocation("Bureau IT");
                laptop1.setCapacity(1);
                resourceRepository.save(laptop1);
                
                Resource projecteur = new Resource();
                projecteur.setName("Vidéoprojecteur Epson");
                projecteur.setDescription("Projecteur HD pour présentations");
                projecteur.setType(ResourceType.EQUIPMENT);
                projecteur.setStatus(ResourceStatus.AVAILABLE);
                projecteur.setLocation("Bureau IT");
                projecteur.setCapacity(1);
                resourceRepository.save(projecteur);
                
                Resource camera = new Resource();
                camera.setName("Caméra Logitech 4K");
                camera.setDescription("Caméra pour visioconférences");
                camera.setType(ResourceType.EQUIPMENT);
                camera.setStatus(ResourceStatus.AVAILABLE);
                camera.setLocation("Bureau IT");
                camera.setCapacity(1);
                resourceRepository.save(camera);
                
                // VÉHICULES
                Resource voiture1 = new Resource();
                voiture1.setName("Renault Clio");
                voiture1.setDescription("Véhicule de service compact");
                voiture1.setType(ResourceType.VEHICLE);
                voiture1.setStatus(ResourceStatus.AVAILABLE);
                voiture1.setLocation("Parking B - Place 12");
                voiture1.setCapacity(5);
                resourceRepository.save(voiture1);
                
                Resource voiture2 = new Resource();
                voiture2.setName("Peugeot 3008");
                voiture2.setDescription("SUV pour déplacements professionnels");
                voiture2.setType(ResourceType.VEHICLE);
                voiture2.setStatus(ResourceStatus.AVAILABLE);
                voiture2.setLocation("Parking B - Place 15");
                voiture2.setCapacity(5);
                resourceRepository.save(voiture2);
                
                Resource camionnette = new Resource();
                camionnette.setName("Ford Transit");
                camionnette.setDescription("Camionnette pour transport de matériel");
                camionnette.setType(ResourceType.VEHICLE);
                camionnette.setStatus(ResourceStatus.AVAILABLE);
                camionnette.setLocation("Parking C");
                camionnette.setCapacity(3);
                resourceRepository.save(camionnette);

                // SALLE SUPPLÉMENTAIRE
                Resource salle4 = new Resource();
                salle4.setName("Salle de Formation");
                salle4.setDescription("Salle équipée pour formations et ateliers");
                salle4.setType(ResourceType.MEETING_ROOM);
                salle4.setStatus(ResourceStatus.AVAILABLE);
                salle4.setLocation("Bâtiment C - Étage 1");
                salle4.setCapacity(25);
                resourceRepository.save(salle4);

                // ÉQUIPEMENT SUPPLÉMENTAIRE
                Resource imprimante = new Resource();
                imprimante.setName("Imprimante HP LaserJet");
                imprimante.setDescription("Imprimante réseau noir et blanc");
                imprimante.setType(ResourceType.EQUIPMENT);
                imprimante.setStatus(ResourceStatus.AVAILABLE);
                imprimante.setLocation("Open Space - Bâtiment A");
                imprimante.setCapacity(1);
                resourceRepository.save(imprimante);

                // VÉHICULE SUPPLÉMENTAIRE
                Resource moto = new Resource();
                moto.setName("Yamaha MT-07");
                moto.setDescription("Moto pour déplacements rapides en ville");
                moto.setType(ResourceType.VEHICLE);
                moto.setStatus(ResourceStatus.AVAILABLE);
                moto.setLocation("Parking A - Place Moto");
                moto.setCapacity(2);
                resourceRepository.save(moto);
                
                logger.info("{} ressources créées", resourceRepository.count());
            } else {
                logger.info(" Des ressources existent déjà ({} ressources)", resourceRepository.count());
            }
            
            logger.info("========================================");
            logger.info(" Initialisation terminée !");
            logger.info("========================================");
            logger.info("");
            logger.info(" COMPTES DE TEST DISPONIBLES :");
            logger.info("   ADMIN    : admin    / admin123");
            logger.info("   MANAGER  : manager  / manager123");
            logger.info("   USER     : user     / user123");
            logger.info("   USER     : john     / john123");
            logger.info("");
        };
    }
}