package org.example.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDateTime support
        objectMapper.registerModule(new JavaTimeModule());
        
        // Register existing module for AAS types
        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(LangStringTextType.class, DefaultLangStringTextType.class);
        module.addAbstractTypeMapping(AssetInformation.class, DefaultAssetInformation.class);
        module.addAbstractTypeMapping(AdministrativeInformation.class, DefaultAdministrativeInformation.class);
        module.addAbstractTypeMapping(Reference.class, DefaultReference.class);
        module.addAbstractTypeMapping(Key.class, DefaultKey.class);
        module.addAbstractTypeMapping(Qualifier.class, DefaultQualifier.class);
        objectMapper.addMixIn(SubmodelElement.class, SubmodelElementMixin.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(module);
        
        return objectMapper;
    }
}
