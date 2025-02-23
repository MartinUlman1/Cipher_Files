package cz.ntt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        log.info("Starting application");
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
        log.info("Stopping application");

    }

}

