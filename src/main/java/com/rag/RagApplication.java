package com.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación RAG (Retrieval-Augmented Generation).
 * 
 * Esta es la clase de entrada del proyecto. Cuando ejecutas "mvn spring-boot:run",
 * este es el método main() que se ejecuta.
 * 
 * @SpringBootApplication es una anotación que combina:
 * - @Configuration: Indica que esta clase contiene configuración de Spring
 * - @EnableAutoConfiguration: Activa la configuración automática de Spring Boot
 * - @ComponentScan: Escanea el paquete com.rag y subpaquetes buscando componentes
 * 
 * Spring Boot automáticamente:
 * 1. Escanea todas las clases con @RestController, @Service, @Repository
 * 2. Configura la base de datos según application.yml
 * 3. Configura Spring AI (Ollama, pgvector)
 * 4. Inicia el servidor web en el puerto 8080
 * 5. Expone los endpoints REST definidos en los controladores
 */
@SpringBootApplication
public class RagApplication {
    
    /**
     * Método principal que inicia la aplicación Spring Boot.
     * 
     * Cuando ejecutas este método:
     * 1. Spring Boot lee application.yml
     * 2. Se conecta a PostgreSQL (localhost:5432/rag_db)
     * 3. Se conecta a Ollama (localhost:11434)
     * 4. Inicializa todos los servicios (@Service)
     * 5. Inicializa todos los controladores (@RestController)
     * 6. Inicia el servidor web en http://localhost:8080
     * 7. Queda esperando peticiones HTTP
     * 
     * @param args Argumentos de línea de comandos (no se usan en este proyecto)
     * 
     * Para ejecutar:
     * - Desde terminal: mvn spring-boot:run
     * - Desde IDE: Run → RagApplication
     * 
     * Para detener:
     * - Presiona Ctrl+C en la terminal
     * 
     * Logs que verás al iniciar:
     * - "Starting RagApplication..."
     * - "Tomcat started on port(s): 8080"
     * - "Started RagApplication in X.XXX seconds"
     */
    public static void main(String[] args) {
        // SpringApplication.run() inicia toda la aplicación
        // RagApplication.class → Le dice a Spring cuál es la clase principal
        // args → Pasa los argumentos de línea de comandos (si los hay)
        SpringApplication.run(RagApplication.class, args);
    }
}
