package com.autorabit.pipeline.config;

import com.autorabit.pipeline.model.Pipeline;
import com.autorabit.pipeline.model.User;
import com.autorabit.pipeline.repository.PipelineRepository;
import com.autorabit.pipeline.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

/**
 * Seeds the database with demo data on startup.
 * Creates default users (admin/developer/viewer) and sample pipelines.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PipelineRepository pipelineRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PipelineRepository pipelineRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pipelineRepository = pipelineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            seedUsers();
            seedPipelines();
            log.info("✅ Demo data seeded successfully");
            log.info("=================================================");
            log.info("  AutoRABIT Pipeline Monitor is READY!");
            log.info("  URL        : http://localhost:8080");
            log.info("  H2 Console : http://localhost:8080/h2-console");
            log.info("  Health     : http://localhost:8080/actuator/health");
            log.info("  WebSocket  : ws://localhost:8080/ws");
            log.info("-------------------------------------------------");
            log.info("  Admin login: admin / admin123");
            log.info("  Dev login  : devuser / dev123");
            log.info("  Viewer     : viewer / view123");
            log.info("=================================================");
        };
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        List<User> users = List.of(
            User.builder()
                .username("admin")
                .email("admin@autorabit.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin User")
                .avatarUrl("https://ui-avatars.com/api/?name=Admin+User&background=6366f1&color=fff")
                .roles(Set.of("ADMIN", "DEVELOPER", "VIEWER"))
                .active(true)
                .build(),

            User.builder()
                .username("devuser")
                .email("dev@autorabit.com")
                .password(passwordEncoder.encode("dev123"))
                .fullName("Dev User")
                .avatarUrl("https://ui-avatars.com/api/?name=Dev+User&background=0ea5e9&color=fff")
                .roles(Set.of("DEVELOPER", "VIEWER"))
                .active(true)
                .build(),

            User.builder()
                .username("viewer")
                .email("viewer@autorabit.com")
                .password(passwordEncoder.encode("view123"))
                .fullName("View Only")
                .avatarUrl("https://ui-avatars.com/api/?name=View+Only&background=10b981&color=fff")
                .roles(Set.of("VIEWER"))
                .active(true)
                .build()
        );

        userRepository.saveAll(users);
        log.info("Seeded {} demo users", users.size());
    }

    private void seedPipelines() {
        if (pipelineRepository.count() > 0) return;

        List<Pipeline> pipelines = List.of(
            Pipeline.builder()
                .name("Salesforce Production Deploy")
                .description("Full deployment pipeline to Salesforce Production org with validation & tests")
                .type(Pipeline.PipelineType.SALESFORCE_DEPLOY)
                .repository("https://github.com/autorabit/sf-deploy-pipeline")
                .branchName("main")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy("admin")
                .build(),

            Pipeline.builder()
                .name("Metadata Backup — Nightly")
                .description("Automated nightly backup of all Salesforce metadata to version control")
                .type(Pipeline.PipelineType.METADATA_BACKUP)
                .repository("https://github.com/autorabit/sf-metadata-backup")
                .branchName("main")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy("admin")
                .build(),

            Pipeline.builder()
                .name("Code Quality Scan — PR Check")
                .description("Static code analysis and PMD rule check triggered on every PR")
                .type(Pipeline.PipelineType.CODE_QUALITY_SCAN)
                .repository("https://github.com/autorabit/sf-code-quality")
                .branchName("develop")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy("devuser")
                .build(),

            Pipeline.builder()
                .name("Sandbox Data Migration")
                .description("Migrate masked production data to QA sandbox environment")
                .type(Pipeline.PipelineType.DATA_MIGRATION)
                .repository("https://github.com/autorabit/sf-data-migration")
                .branchName("release/2.0")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy("devuser")
                .build(),

            Pipeline.builder()
                .name("Release Manager — Sprint 42")
                .description("Coordinated release pipeline across dev, qa, staging to production")
                .type(Pipeline.PipelineType.RELEASE_MANAGER)
                .repository("https://github.com/autorabit/sf-release-manager")
                .branchName("release/sprint-42")
                .status(Pipeline.PipelineStatus.IDLE)
                .createdBy("admin")
                .build()
        );

        pipelineRepository.saveAll(pipelines);
        log.info("Seeded {} demo pipelines", pipelines.size());
    }
}
