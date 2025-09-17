package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.eclipse.digitaltwin.basyx.aasenvironment.component.AasEnvironmentComponent;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration.class
})
public class AASServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AASServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner startBaSyxEnvironment() {
        System.setProperty("server.port", "8081");
        return (AasEnvironmentComponent::main);
    }

}
