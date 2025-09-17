package org.example.database;

import org.eclipse.digitaltwin.basyx.aasrepository.AasRepositoryFactory;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.ConceptDescriptionRepository;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.backend.ConceptDescriptionBackend;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.backend.CrudConceptDescriptionRepositoryFactory;
import org.eclipse.digitaltwin.basyx.conceptdescriptionrepository.backend.InMemoryConceptDescriptionBackend;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.eclipse.digitaltwin.basyx.aasrepository.backend.CrudAasRepositoryFactory;

@Configuration
public class BaSyxConfig {

    @Bean
    public AasRepositoryFactory aasRepositoryFactory(){
        return CrudAasRepositoryFactory.builder().buildFactory();
    }

    @Bean
    public ConnectedAasRepository aasRepository(){
        return new ConnectedAasRepository("http://localhost:8081");
    }

    @Bean
    public ConnectedSubmodelRepository submodelRepository() {
        return new ConnectedSubmodelRepository("http://localhost:8081");
    }

    private final ConceptDescriptionBackend conceptDescriptionBackend = new InMemoryConceptDescriptionBackend();

    @Bean
    public ConceptDescriptionRepository conceptDescriptionRepository() {
    	return CrudConceptDescriptionRepositoryFactory.builder().backend(conceptDescriptionBackend).create();
    }
}
