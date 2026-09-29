package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.dto.CategoryRequest;
import com.codewithpaolo.frischcheck.dto.CategoryResponse;
import com.codewithpaolo.frischcheck.exception.BusinessRuleException;
import com.codewithpaolo.frischcheck.exception.ResourceNotFoundException;
import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        validateThresholds(request);
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("Kategorie '" + request.name() + "' existiert bereits");
        }
        Category category = new Category(request.name().trim(), request.warningDays(), request.criticalDays());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        validateThresholds(request);
        Category category = getEntity(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BusinessRuleException("Kategorie '" + request.name() + "' existiert bereits");
        }
        category.setName(request.name().trim());
        category.setWarningDays(request.warningDays());
        category.setCriticalDays(request.criticalDays());
        return CategoryResponse.from(category);
    }

    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie", id));
    }

    private void validateThresholds(CategoryRequest request) {
        if (request.criticalDays() > request.warningDays()) {
            throw new BusinessRuleException("criticalDays darf nicht größer als warningDays sein");
        }
    }
}
