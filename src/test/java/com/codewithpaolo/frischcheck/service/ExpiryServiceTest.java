package com.codewithpaolo.frischcheck.service;

import com.codewithpaolo.frischcheck.model.Category;
import com.codewithpaolo.frischcheck.model.ExpiryLevel;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiryServiceTest {

    static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    static final LocalDate TODAY = LocalDate.of(2026, 9, 29);

    private final Category frischware = new Category("Frischware", 5, 2);
    private final Category getraenke = new Category("Getränke", 60, 30);

    @Mock
    private ProductRepository productRepository;

    private ExpiryService expiryService;

    @BeforeEach
    void setUp() {
        expiryService = new ExpiryService(productRepository, fixedClock());
    }

    static Clock fixedClock() {
        return Clock.fixed(TODAY.atTime(8, 0).atZone(ZONE).toInstant(), ZONE);
    }

    static Product product(String name, Category category, LocalDate expiryDate) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setExpiryDate(expiryDate);
        product.setStatus(ProductStatus.AUF_LAGER);
        return product;
    }

    @ParameterizedTest(name = "Frischware (Warnung 5, kritisch 2): {0} Tage bis Ablauf -> {1}")
    @CsvSource({
            "-3, ABGELAUFEN",
            "-1, ABGELAUFEN",
            "0,  KRITISCH",
            "2,  KRITISCH",
            "3,  WARNUNG",
            "5,  WARNUNG",
            "6,  OK",
            "30, OK"
    })
    void evaluate_appliesCategoryThresholdsIncludingBoundaries(int daysFromToday, ExpiryLevel expected) {
        Product milk = product("Milch", frischware, TODAY.plusDays(daysFromToday));

        assertThat(expiryService.evaluate(milk)).isEqualTo(expected);
    }

    @Test
    void evaluate_sameDateGivesDifferentLevelPerCategory() {
        LocalDate inTwentyDays = TODAY.plusDays(20);

        assertThat(expiryService.evaluate(product("Salat", frischware, inTwentyDays))).isEqualTo(ExpiryLevel.OK);
        assertThat(expiryService.evaluate(product("Cola", getraenke, inTwentyDays))).isEqualTo(ExpiryLevel.KRITISCH);
    }

    @Test
    void daysUntilExpiry_isNegativeForExpiredProducts() {
        assertThat(expiryService.daysUntilExpiry(product("Joghurt", frischware, TODAY.minusDays(4)))).isEqualTo(-4);
        assertThat(expiryService.daysUntilExpiry(product("Joghurt", frischware, TODAY))).isZero();
    }

    @Test
    void findExpiringProducts_filtersOkProductsAndKeepsFefoOrder() {
        Product expired = product("Hackfleisch", frischware, TODAY.minusDays(1));
        Product critical = product("Sahne", frischware, TODAY.plusDays(1));
        Product fine = product("Karotten", frischware, TODAY.plusDays(10));
        Product warningDrink = product("Apfelsaft", getraenke, TODAY.plusDays(45));
        // Repository liefert bereits nach Ablaufdatum sortiert (FEFO)
        when(productRepository.findByStatusOrderByExpiryDateAscNameAsc(ProductStatus.AUF_LAGER))
                .thenReturn(List.of(expired, critical, fine, warningDrink));

        List<Product> result = expiryService.findExpiringProducts();

        assertThat(result).containsExactly(expired, critical, warningDrink);
    }

    @ParameterizedTest(name = "{0} -> \"{1}\"")
    @CsvSource({
            "-5, seit 5 Tagen abgelaufen",
            "-1, seit gestern abgelaufen",
            "0,  läuft heute ab",
            "1,  läuft morgen ab",
            "4,  läuft in 4 Tagen ab"
    })
    void describeDays_returnsReadableText(long days, String expected) {
        assertThat(ExpiryService.describeDays(days)).isEqualTo(expected);
    }
}
