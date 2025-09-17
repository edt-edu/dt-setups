package de.unistuttgart.isw.dtservices.weather;

@SpringBootApplication()
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.unistuttgart.isw.weather", "de.unistuttgart.isw.dtservices"})
public class WeatherService {

    public static void main(String[] args) {
        Runner.main(args);
    }
}
