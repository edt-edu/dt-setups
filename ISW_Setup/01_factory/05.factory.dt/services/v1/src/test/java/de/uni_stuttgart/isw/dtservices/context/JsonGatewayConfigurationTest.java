package de.uni_stuttgart.isw.dtservices.context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import de.unistuttgart.isw.dtservices.services.configuration.JsonGatewayConfiguration;
 
public class JsonGatewayConfigurationTest {
    
    @Test
    public void testJsonGatewayConfiguration(){

        // load json from test resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/example_declaration.json");

        String json = new BufferedReader(new InputStreamReader(is))
            .lines().reduce("", (accumulator, actual) -> accumulator + actual);

        JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(json);

        assert jsonGatewayConfiguration.getGatewayContext() != null;

    }
}
