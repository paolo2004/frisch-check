package com.codewithpaolo.frischcheck.config;

import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legt beim Start die in der application.yml definierten Kategorien an.
 * Bereits vorhandene Kategorien werden nicht überschrieben (Änderungen per API bleiben erhalten).
 */
@Component
public class CategoryDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CategoryDataInitializer.class);

    private final ExpiryProperties expiryProperties;
    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(ExpiryProperties expiryProperties, CategoryRepository categoryRepository) {
        this.expiryProperties = expiryProperties;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (ExpiryProperties.CategoryThreshold threshold : expiryProperties.categories()) {
            if (!categoryRepository.existsByNameIgnoreCase(threshold.name())) {
                categoryRepository.save(new Category(threshold.name(), threshold.warningDays(), threshold.criticalDays()));
                log.info("Kategorie angelegt: {} (Warnung {} Tage, kritisch {} Tage)",
                        threshold.name(), threshold.warningDays(), threshold.criticalDays());
            }
        }
    }
}
