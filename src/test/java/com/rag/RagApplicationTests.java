package com.rag;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.ai.vectorstore.pgvector.initialize-schema=false"
})
class RagApplicationTests {

    @Test
    void contextLoads() {
        // Test básico para verificar que el contexto de Spring carga correctamente
    }
}
