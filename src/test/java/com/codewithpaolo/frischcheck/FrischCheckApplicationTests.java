package com.codewithpaolo.frischcheck;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Startet den kompletten Spring-Kontext einmal – findet Konfigurationsfehler (Beans, YAML, JPA-Mapping).
 * Nutzt eine In-Memory-H2, damit Tests die Entwicklungs-DB unter ./data nicht anfassen.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class FrischCheckApplicationTests {

    @Test
    void contextLoads() {
    }

}
