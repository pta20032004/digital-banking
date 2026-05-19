package com.tony.demo.infra.init;

import com.tony.demo.modules.notification.domain.NotificationTemplate;
import com.tony.demo.modules.notification.domain.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class NotificationTemplateSeeder implements CommandLineRunner {

    private final NotificationTemplateRepository repository;

    @Value("${notification.templates.deposit.code}")
    private String depositCode;
    @Value("${notification.templates.deposit.titleVi}")
    private String depositTitleVi;
    @Value("${notification.templates.deposit.titleEn}")
    private String depositTitleEn;
    @Value("${notification.templates.deposit.contentVi}")
    private String depositContentVi;
    @Value("${notification.templates.deposit.contentEn}")
    private String depositContentEn;

    @Value("${notification.templates.maintain.code}")
    private String maintainCode;
    @Value("${notification.templates.maintain.titleVi}")
    private String maintainTitleVi;
    @Value("${notification.templates.maintain.titleEn}")
    private String maintainTitleEn;
    @Value("${notification.templates.maintain.contentVi}")
    private String maintainContentVi;
    @Value("${notification.templates.maintain.contentEn}")
    private String maintainContentEn;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding Notification Templates...");

        if (repository.count() == 0) {
            NotificationTemplate depositTemplate = NotificationTemplate.builder()
                    .code(depositCode)
                    .titleVi(depositTitleVi)
                    .titleEn(depositTitleEn)
                    .contentVi(depositContentVi)
                    .contentEn(depositContentEn)
                    .build();

            NotificationTemplate maintainTemplate = NotificationTemplate.builder()
                    .code(maintainCode)
                    .titleVi(maintainTitleVi)
                    .titleEn(maintainTitleEn)
                    .contentVi(maintainContentVi)
                    .contentEn(maintainContentEn)
                    .build();

            repository.saveAll(List.of(depositTemplate, maintainTemplate));
            log.info("Successfully seeded Notification Templates.");
        } else {
            log.info("Notification Templates already exist. Skipping seed.");
        }
    }
}
