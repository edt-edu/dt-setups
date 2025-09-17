package org.example.database;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DockerFilesBootstrap {

    @Bean
    CommandLineRunner preloadDockerFiles(
            GridFsTemplate gridFsTemplate,
            DockerRepository dockerRepo
    ) {
        return toBeReturned -> {
            // find all GridFS entries tagged with "dockerFileName" + "aasIdShort"
            List<GridFSFile> allFiles = gridFsTemplate.find(
                    Query.query(Criteria.where("metadata.aasIdShort").exists(true))
            ).into(new ArrayList<>());

            for (GridFSFile file : allFiles) {
                try {
                    String aasIdShort = (String) Objects.requireNonNull(file.getMetadata()).get("aasIdShort");
                    String dockerName = file.getFilename();

                    // this writes it out to dockerfiles/{aasIdShort}/{dockerName}
                    dockerRepo.getDockerFile(aasIdShort, dockerName);
                } catch (Exception e) {
                    System.err.println("Error preloading Docker file: " + file.getFilename() + " - " + e.getMessage());
                    // Continue with other files instead of failing the entire bootstrap
                }
            }
        };
    }
}
