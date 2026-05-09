package com.aiquiz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AiQuizGeneratorApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the Spring application context loads successfully
        // with all beans, security config, and JPA repositories wired correctly.
    }
}
