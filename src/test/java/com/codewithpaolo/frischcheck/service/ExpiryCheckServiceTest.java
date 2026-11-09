package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.dto.ExpiryCheckResult;
import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;
import com.codewithpaolo.frischcheck.repository.ProductRepository;
import com.codewithpaolo.frischcheck.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static com.codewithpaolo.frischcheck.service.ExpiryServiceTest.TODAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiryCheckServiceTest {

    private final Category frischware = new Category("Frischware", 5, 2);

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;

    private ExpiryCheckService expiryCheckService;

    @BeforeEach
    void setUp() {
        Clock clock = ExpiryServiceTest.fixedClock();
        expiryCheckService = new ExpiryCheckService(new ExpiryService(productRepository, clock),
                notificationService, clock);
    }

    private Product product(String name, LocalDate expiryDate) {
        Product product = ExpiryServiceTest.product(name, frischware, expiryDate);
        product.setQuantity(new BigDecimal("2.50"));
        product.setUnit(Unit.KG);
        product.setStorageLocation(StorageLocation.KUEHLSCHRANK);
        return product;
    }

    @Test
    void runCheck_withoutExpiringProducts_sendsNoNotification() {
        when(productRepository.findByStatusOrderByExpiryDateAscNameAsc(ProductStatus.AUF_LAGER))
                .thenReturn(List.of(product("Karotten", TODAY.plusDays(20))));

        ExpiryCheckResult result = expiryCheckService.runCheck();

        assertThat(result.expiringProducts()).isZero();
        verifyNoInteractions(notificationService);
    }

    @Test
    void runCheck_sendsOneMessageGroupedByLevel() {
        when(productRepository.findByStatusOrderByExpiryDateAscNameAsc(ProductStatus.AUF_LAGER))
                .thenReturn(List.of(
                        product("Hackfleisch", TODAY.minusDays(2)),
                        product("Sahne", TODAY.plusDays(1)),
                        product("Salat", TODAY.plusDays(4)),
                        product("Karotten", TODAY.plusDays(20))));
        when(notificationService.notifyAll(anyString(), anyString())).thenReturn(2);

        ExpiryCheckResult result = expiryCheckService.runCheck();

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifyAll(eq("Ablaufwarnung: 3 Produkt(e) brauchen Aufmerksamkeit"),
                message.capture());

        assertThat(message.getValue())
                .contains("Stand: 29.09.2026")
                .contains("ABGELAUFEN (1)\n- Hackfleisch (Frischware) – 2.5 kg – Kühlschrank – seit 2 Tagen abgelaufen")
                .contains("KRITISCH (1)\n- Sahne")
                .contains("WARNUNG (1)\n- Salat")
                .doesNotContain("Karotten");
        // Reihenfolge: das Dringendste zuerst
        assertThat(message.getValue().indexOf("ABGELAUFEN"))
                .isLessThan(message.getValue().indexOf("KRITISCH"));
        assertThat(result.expiringProducts()).isEqualTo(3);
        assertThat(result.channelsNotified()).isEqualTo(2);
    }
}
