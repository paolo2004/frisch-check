package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.dto.ProductRequest;
import com.codewithpaolo.frischcheck.dto.ProductResponse;
import com.codewithpaolo.frischcheck.exception.BusinessRuleException;
import com.codewithpaolo.frischcheck.exception.ResourceNotFoundException;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ExpiryService expiryService;
    private final Clock clock;

    public ProductService(ProductRepository productRepository, CategoryService categoryService,
                          ExpiryService expiryService, Clock clock) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.expiryService = expiryService;
        this.clock = clock;
    }

    /** Alle Produkte (optional nach Status gefiltert), sortiert nach FEFO. */
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(ProductStatus status) {
        List<Product> products = status == null
                ? productRepository.findAllByOrderByExpiryDateAscNameAsc()
                : productRepository.findByStatusOrderByExpiryDateAscNameAsc(status);
        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findExpiring() {
        return expiryService.findExpiringProducts().stream().map(this::toResponse).toList();
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        product.setStatus(ProductStatus.AUF_LAGER);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        ensureInStock(product);
        applyRequest(product, request);
        return toResponse(product);
    }

    /**
     * Markiert ein Produkt als VERBRAUCHT oder WEGGEWORFEN. Produkte werden nie gelöscht,
     * und ein abgeschlossenes Produkt kann nicht erneut geändert werden.
     */
    public ProductResponse changeStatus(Long id, ProductStatus newStatus) {
        if (newStatus == ProductStatus.AUF_LAGER) {
            throw new BusinessRuleException("Ein Produkt kann nicht zurück auf AUF_LAGER gesetzt werden");
        }
        Product product = getEntity(id);
        ensureInStock(product);
        product.setStatus(newStatus);
        product.setStatusChangedAt(LocalDateTime.now(clock));
        return toResponse(product);
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt", id));
    }

    private void ensureInStock(Product product) {
        if (product.getStatus() != ProductStatus.AUF_LAGER) {
            throw new BusinessRuleException("Produkt " + product.getId() + " ist bereits "
                    + product.getStatus() + " und kann nicht mehr geändert werden");
        }
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setCategory(categoryService.getEntity(request.categoryId()));
        product.setStorageLocation(request.storageLocation());
        product.setQuantity(request.quantity());
        product.setUnit(request.unit());
        product.setExpiryDate(request.expiryDate());
        product.setReceivedDate(request.receivedDate() != null ? request.receivedDate() : LocalDate.now(clock));
    }

    private ProductResponse toResponse(Product product) {
        long daysLeft = expiryService.daysUntilExpiry(product);
        return ProductResponse.from(product, daysLeft, expiryService.evaluate(product),
                ExpiryService.describeDays(daysLeft));
    }
}
