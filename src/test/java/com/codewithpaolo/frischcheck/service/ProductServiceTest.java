package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.dto.ProductRequest;
import com.codewithpaolo.frischcheck.dto.ProductResponse;
import com.codewithpaolo.frischcheck.exception.BusinessRuleException;
import com.codewithpaolo.frischcheck.exception.ResourceNotFoundException;
import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.model.ExpiryLevel;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;
import com.codewithpaolo.frischcheck.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static com.codewithpaolo.frischcheck.service.ExpiryServiceTest.TODAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private final Category frischware = new Category("Frischware", 5, 2);

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryService categoryService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        Clock clock = ExpiryServiceTest.fixedClock();
        // echte ExpiryService: reine Berechnungslogik, kein Grund zu mocken
        ExpiryService expiryService = new ExpiryService(productRepository, clock);
        productService = new ProductService(productRepository, categoryService, expiryService, clock);
    }

    private ProductRequest request(LocalDate receivedDate) {
        return new ProductRequest("  Sahne ", 1L, StorageLocation.KUEHLSCHRANK, new BigDecimal("3"),
                Unit.LITER, TODAY.plusDays(1), receivedDate);
    }

    @Test
    void create_setsInStockAndDefaultsReceivedDateToToday() {
        when(categoryService.getEntity(1L)).thenReturn(frischware);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request(null));

        assertThat(response.name()).isEqualTo("Sahne");
        assertThat(response.status()).isEqualTo(ProductStatus.AUF_LAGER);
        assertThat(response.receivedDate()).isEqualTo(TODAY);
        assertThat(response.daysUntilExpiry()).isEqualTo(1);
        assertThat(response.expiryLevel()).isEqualTo(ExpiryLevel.KRITISCH);
        assertThat(response.expiryText()).isEqualTo("läuft morgen ab");
    }

    @Test
    void create_withUnknownCategory_throwsNotFoundAndSavesNothing() {
        when(categoryService.getEntity(1L)).thenThrow(new ResourceNotFoundException("Kategorie", 1L));

        assertThatThrownBy(() -> productService.create(request(TODAY)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void changeStatus_marksProductAsDiscardedWithTimestamp() {
        Product product = ExpiryServiceTest.product("Sahne", frischware, TODAY.minusDays(1));
        product.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.changeStatus(7L, ProductStatus.WEGGEWORFEN);

        assertThat(response.status()).isEqualTo(ProductStatus.WEGGEWORFEN);
        assertThat(product.getStatusChangedAt()).isEqualTo(LocalDateTime.of(TODAY, LocalTime.of(8, 0)));
    }

    @Test
    void changeStatus_onAlreadyFinishedProduct_isRejected() {
        Product product = ExpiryServiceTest.product("Sahne", frischware, TODAY);
        product.setId(7L);
        product.setStatus(ProductStatus.VERBRAUCHT);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.changeStatus(7L, ProductStatus.WEGGEWORFEN))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("bereits VERBRAUCHT");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.VERBRAUCHT);
    }

    @Test
    void changeStatus_backToInStock_isRejected() {
        assertThatThrownBy(() -> productService.changeStatus(7L, ProductStatus.AUF_LAGER))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findById_unknownId_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produkt mit ID 99 wurde nicht gefunden");
    }
}
