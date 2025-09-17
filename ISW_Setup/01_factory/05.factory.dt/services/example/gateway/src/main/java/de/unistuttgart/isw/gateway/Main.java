package de.unistuttgart.isw.gateway;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.unistuttgart.isw.dtservices.Runner;


@SpringBootApplication()
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.unistuttgart.isw.gateway", "de.unistuttgart.isw.dtservices"})
public class Main {

    public static void main(String[] args) {
        Runner.main(args);
    }
}
