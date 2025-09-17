package org.example.aasManager;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.example.database.DockerRepository;
import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;



@Service
public class DockerOptionsHandler {
    private final DockerClient dockerClient;
    private final List<String> runningContainers = Collections.synchronizedList(new ArrayList<>());
//    private static final String COUNTER_SUBMODEL_ID = "docker-counter";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    private final DockerRepository dockerRepository;
    private final ObjectMapper MAPPER = new ObjectMapper();


    public DockerOptionsHandler(DockerRepository dockerRepository) {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(detectDockerHost())
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(new OkDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .build())
                .build();
        this.dockerRepository = dockerRepository;
    }

    private String detectDockerHost() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "npipe:////./pipe/docker_engine";
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            return "unix:///var/run/docker.sock";
        }
        throw new IllegalStateException("Unsupported operating system");
    }

    public void applyContainerOptions(Submodel sm, AASManager aasManager, DockerRepository dockerRepo, String aasId) throws URISyntaxException {
        String aasIdShort = aasManager.getAAS(aasId).getIdShort();
        String execTrigger = getOpt(sm, "executionTrigger");
        String dockerContextName = getOpt(sm,"dockerSource");
        String dockerURL = null;
        if(dockerContextName==null)
            dockerURL = getOpt(sm,"dockerURL");
        switch (execTrigger){
            case "onInitialize":
                if(dockerContextName != null){ //if dockerContextName is not null then use it
                    try{
                        Path dockerContextPath = dockerRepo.getDockerFile(aasIdShort, dockerContextName);
                        doDockerStart(aasIdShort, dockerContextPath, sm);
                    }catch (IOException e){
                        throw new AASOperationException("Error starting Docker container: " + e.getMessage());
                    }catch (NotModifiedException e) {
                        // Container already in desired state, not an error
                        System.out.println("Container already in desired state for " + aasIdShort + "/" + dockerContextName);
                    }
                }else{ //otherwise assume dockerURL is not null and download the dockerfile from there
                    executeDockerURL(sm, aasIdShort, dockerURL);
                }
                break;
            case "onUpdate":
                if(dockerContextName != null){ //if dockerContextName is not null then use it
                    try{
                        cleanupOldContainers(aasIdShort, dockerContextName);
                        Path dockerContextPath = dockerRepo.getDockerFile(aasIdShort, dockerContextName);
                        doDockerStart(aasIdShort, dockerContextPath, sm);
                    }catch (IOException e){
                        throw new AASOperationException("Error starting Docker container: " + e.getMessage());
                    }catch (NotModifiedException e) {
                        // Container already in desired state, not an error
                        System.out.println("Container already in desired state for " + aasIdShort + "/" + dockerContextName);
                    }
                }else {
                    URI uri = new URI(dockerURL);
                    dockerContextName = Paths.get(uri.getPath()).getFileName().toString();
                    cleanupOldContainers(aasIdShort, dockerContextName);
                    executeDockerURL(sm, aasIdShort, dockerURL);
                }
                break;
            case "onDemand":
            case "onAccess":
                if(dockerContextName != null){ //if dockerContextName is not null then use it
                    try{
                        List <Container> running = isContainerRunning(aasIdShort, dockerContextName);
                        Path dockerContextPath = dockerRepo.getDockerFile(aasIdShort, dockerContextName);
                        if(running.isEmpty()) {
                            doDockerStart(aasIdShort, dockerContextPath, sm);
                        }else{
                            String containerId = running.getFirst().getId();
                            HealthStatus health = getContainerHealth(containerId);
                            if(health == HealthStatus.UNHEALTHY){
                                cleanupOldContainers(aasIdShort, dockerContextName);
                                doDockerStart(aasIdShort, dockerContextPath, sm);
                            }
                        }
                    }catch (IOException e){
                        throw new AASOperationException("Error starting Docker container: " + e.getMessage());
                    }catch (NotModifiedException e) {
                        // Container already in desired state, not an error
                        System.out.println("Container already in desired state for " + aasIdShort + "/" + dockerContextName);
                    }
                }else {
                    URI uri = new URI(dockerURL);
                    dockerContextName = Paths.get(uri.getPath()).getFileName().toString();
                    List<Container> running = isContainerRunning(aasIdShort, dockerContextName);
                    if(running.isEmpty()){
                        executeDockerURL(sm, aasIdShort, dockerURL);
                    }else{
                        String containerId = running.getFirst().getId();
                        HealthStatus health = getContainerHealth(containerId);
                        if(health == HealthStatus.UNHEALTHY){
                            cleanupOldContainers(aasIdShort, dockerContextName);
                            executeDockerURL(sm, aasIdShort, dockerURL);
                        }
                    }
                }

                break;
            default:
                throw new AASOperationException("Invalid execution trigger for the Docker context");
        }
    }


    private List<Container> isContainerRunning(String aasIdShort, String dockerContextName) {
        Map<String,String> filter = Map.of("aasIdShort", aasIdShort, "dockerFile", dockerContextName);
        return dockerClient.listContainersCmd()
                .withLabelFilter(filter)
                .withStatusFilter(List.of("running"))
                .exec();
    }

    private void executeDockerURL(Submodel sm, String aasIdShort, String dockerURL) {
        if(dockerURL == null || dockerURL.isBlank()){
            throw new AASOperationException("Either dockerSource or dockerURL must be specified");
        }
        try{
            URI uri = new URI(dockerURL);
            URL url = uri.toURL();
            Path dockerContextPath = fetchDockerFileFromURL(url, aasIdShort);
            doDockerStart(aasIdShort, dockerContextPath, sm);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new AASOperationException("Invalid URL in dockerURL property: " + dockerURL, e);
        } catch (IOException e) {
            throw new AASOperationException("Error downloading Docker context from URL: " + dockerURL, e);
        }
    }


    //Retrieves the Options from the DockerOptions submodel
    public String getOpt(Submodel sm, String idShort) {
        return sm.getSubmodelElements().stream()
                .filter(e -> e.getIdShort().equals(idShort) && e instanceof Property)
                .map(e -> ((Property) e).getValue()).findFirst()
                .orElse(null);
    }


    //Starts the docker client and runs the container with the given options and returns the container ID.
    private String buildAndStart(String dockerPathString,
                                String aasIdShort,
                                String dockerContextName,
                                String dockerDataJson) {
        Path tmpCtx = null;
        try {
            Path dockerPath = Paths.get(dockerPathString);
            if (!Files.exists(dockerPath)) {
                throw new AASOperationException("Docker context does not exist: " + dockerPathString);
            }

            boolean isDirectory = Files.isDirectory(dockerPath);
            Path projectPath;
            File dockerFile;
            
            if (isDirectory) {
                // If it's a directory, use it directly as the project path
                projectPath = dockerPath;
                
                // Look for Dockerfile in the directory
                Path possibleDockerfile = projectPath.resolve("Dockerfile");
                if (Files.exists(possibleDockerfile)) {
                    dockerFile = possibleDockerfile.toFile();
                } else {
                    // If no Dockerfile exists, look for any file that starts with "Dockerfile"
                    Optional<Path> anyDockerfile = Files.list(projectPath)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().startsWith("dockerfile"))
                        .findFirst();
                    
                    if (anyDockerfile.isPresent()) {
                        dockerFile = anyDockerfile.get().toFile();
                    } else {
                        throw new AASOperationException("No Dockerfile found in directory: " + dockerPathString);
                    }
                }
            } else {
                // If it's a file, use its parent directory as the project path
                dockerFile = new File(dockerPathString);
                projectPath = dockerFile.getParentFile().toPath();
            }

            // Create temporary build context
            tmpCtx = Files.createTempDirectory("docker-build-ctx-");
            final Path finalTmpCtx = tmpCtx;

            // Copy entire folder to temp directory
            Files.walk(projectPath).forEach(source -> {
                try {
                    Path target = finalTmpCtx.resolve(projectPath.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            // Rename selected Dockerfile to "Dockerfile" if it's not already named that (for Linux compatibility)
            Path originalDockerfile = tmpCtx.resolve(projectPath.relativize(dockerFile.toPath()).toString());
            Path standardDockerfile;
            
            if (!originalDockerfile.getFileName().toString().equals("Dockerfile")) {
                standardDockerfile = tmpCtx.resolve("Dockerfile");
                Files.move(originalDockerfile, standardDockerfile, StandardCopyOption.REPLACE_EXISTING);
                
                // Optional cleanup of other Dockerfiles
                try (Stream<Path> files = Files.walk(tmpCtx)) {
                    files.filter(path -> !Files.isDirectory(path))
                        .filter(path -> path.getFileName().toString().toLowerCase().startsWith("dockerfile"))
                        .filter(path -> !path.equals(standardDockerfile))
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                        });
                }
            } else {
                standardDockerfile = originalDockerfile;
            }

            File buildContext = tmpCtx.toFile();
            String imageTag = (aasIdShort + "-" + dockerContextName + "-" + System.currentTimeMillis())
                                .toLowerCase(Locale.ROOT)
                                .replace('\\', '-')
                                .replace('/', '-');

            String imageID;
            try {
                imageID = dockerClient.buildImageCmd(buildContext)
                        .withDockerfile(standardDockerfile.toFile())
                        .withTags(Set.of(imageTag))
                        .exec(new BuildImageResultCallback())
                        .awaitImageId();
            } catch (NotModifiedException nm) {
                try {
                    imageID = dockerClient.inspectImageCmd(imageTag).exec().getId();
                } catch (Exception e) {
                    imageID = imageTag;
                }
            }

            Map<String, String> labels = Map.of(
                    "aasIdShort", aasIdShort,
                    "dockerFile", dockerContextName
            );

            CreateContainerCmd cmd = dockerClient.createContainerCmd(Objects.requireNonNull(imageID))
                    .withLabels(labels);

            if (dockerDataJson != null) {
                String apiBase = isLinux() 
                ? "http://172.17.0.1:8080/api/aas"
                : "http://host.docker.internal:8080/api/aas";

                cmd.withEnv("DOCKER_DATA=" + dockerDataJson, "AAS_API_BASE=" + apiBase);
            }

            CreateContainerResponse resp = cmd.exec();
            try {
                dockerClient.startContainerCmd(resp.getId()).exec();
            } catch (NotModifiedException ignored) {
                System.out.println("Container already running: " + resp.getId());
            }

            return resp.getId();

        } catch (Exception e) {
            System.err.println("Docker build/start error: " + e.getMessage());
            throw new AASOperationException("Docker build/start error: " + e.getMessage(), e);
        } finally {
            if (tmpCtx != null) {
                try {
                    Files.walk(tmpCtx)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                        });
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    private boolean isLinux(){
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }



    //Increments the counter of the counter submodel by one.
//    public void incrementExecutionCount(AASManager aasManager, String aasId) {
//        Submodel counterSM = getCounterSubmodel(aasManager, aasId); // Get the counter submodel
//
//        //Find the Counter property in the SM
//        Property counterProp = (Property) Objects.requireNonNull(counterSM).getSubmodelElements().stream()
//                .filter(e -> e.getIdShort().equals("executionCount") && e instanceof Property)
//                .findFirst()
//                .orElseThrow(() -> new AASOperationException("Counter property not found"));
//
//        int currentCount = Integer.parseInt(counterProp.getValue());
//        counterProp.setValue(Integer.toString(currentCount+1));
//        aasManager.updateSubmodel(counterSM.getId(),counterSM);
//    }

    public void cleanupOldContainers(String aasIdShort, String dockerContextName) {
        try {
            Map<String,String> filter = Map.of("aasIdShort", aasIdShort, "dockerFile", dockerContextName);
            dockerClient.listContainersCmd()
                    .withLabelFilter(filter)
                    .withShowAll(true)
                    .exec()
                    .forEach(c -> {
                        try {
                            dockerClient.stopContainerCmd(c.getId()).withTimeout(10).exec();
                        } catch (NotModifiedException e) {
                            // Container already stopped, which is fine
                            System.out.println("Container already stopped: " + c.getId());
                        } catch (Exception e) {
                            System.err.println("Error stopping container " + c.getId() + ": " + e.getMessage());
                        }
                        
                        try {
                            dockerClient.removeContainerCmd(c.getId()).withForce(true).exec();
                        } catch (Exception e) {
                            System.err.println("Error removing container " + c.getId() + ": " + e.getMessage());
                        }
                    });
            System.out.println("Cleaned up containers for " + aasIdShort + "/" + dockerContextName);
        } catch (Exception e) {
            System.err.println("Error cleaning up containers: " + e.getMessage());
        }
    }

    private void doDockerStart(String aasIdShort,
                               Path dockerContextPath,
                               Submodel sm) throws IOException {
        // 1) Check existence
        if (!Files.exists(dockerContextPath)) {
            throw new AASOperationException("Docker context not found: " + dockerContextPath);
        }

        String dockerContextName = dockerContextPath.getFileName().toString();
        
        // Check if it's a directory and contains a Dockerfile
        boolean isDirectory = Files.isDirectory(dockerContextPath);
        if (isDirectory) {
            Path dockerfilePath = dockerContextPath.resolve("Dockerfile");
            boolean hasDockerfile = Files.exists(dockerfilePath);
            
            if (!hasDockerfile) {
                // Look for any file that starts with "Dockerfile"
                try (Stream<Path> files = Files.list(dockerContextPath)) {
                    boolean foundDockerfile = files
                        .filter(Files::isRegularFile)
                        .anyMatch(p -> p.getFileName().toString().toLowerCase().startsWith("dockerfile"));
                    
                    if (!foundDockerfile) {
                        throw new AASOperationException("No Dockerfile found in directory: " + dockerContextPath);
                    }
                }
            }
            
            System.out.println("Using Docker context directory: " + dockerContextPath);
        } else {
            System.out.println("Using Docker file: " + dockerContextPath);
        }

        try {
            // First check if there's already a container running for this AAS/Docker context
            Map<String,String> filter = Map.of("aasIdShort", aasIdShort, "dockerFile", dockerContextName);
            List<com.github.dockerjava.api.model.Container> existingContainers = dockerClient.listContainersCmd()
                    .withLabelFilter(filter)
                    .withStatusFilter(List.of("running"))
                    .exec();
            
            if (!existingContainers.isEmpty()) {
                // Container already exists and is running
                String existingContainerId = existingContainers.getFirst().getId();
                System.out.println("Container already running for " + aasIdShort + "/" + dockerContextName + ": " + existingContainerId);
                
                // Add to our tracking list if not already there
                if (!runningContainers.contains(existingContainerId)) {
                    runningContainers.add(existingContainerId);
                }
                
                return;
            }

            // Getting the data docker container needs from the xml file it can be null if the container doesn't need data
            Optional<SubmodelElementCollection> dockerData = sm.getSubmodelElements().stream()
                    .filter(e -> e.getIdShort().equals("dockerData") && e instanceof SubmodelElementCollection)
                    .map(e -> (SubmodelElementCollection)e)
                    .findFirst();

            String dockerDataJson = null;
            if(dockerData.isPresent()){ //If dockerData is present convert it to json string
                dockerDataJson = MAPPER.writeValueAsString(dockerData.get().getValue());
            }

            // ensure we pass Docker-java an absolute path, so it can work in Linux
            String absDockerContextPath = dockerContextPath.toAbsolutePath().toString();
            
            String containerId = buildAndStart(
                    absDockerContextPath,
                    aasIdShort,
                    dockerContextName,
                    dockerDataJson
            );
            
            // Verify the container exists and is running before adding to our list
            boolean containerRunning = !dockerClient.listContainersCmd()
                    .withIdFilter(List.of(containerId))
                    .withStatusFilter(List.of("running"))
                    .exec()
                    .isEmpty();
                    
            if (containerRunning) {
                if (!runningContainers.contains(containerId)) {
                    runningContainers.add(containerId);
                }
                System.out.println("Successfully started container: " + containerId);
            } else {
                System.out.println("Container created but not running: " + containerId);
            }

            //schedule Termination on timeout
            String terminationTrigger = getOpt(sm, "terminationTrigger");
            if ("onTimeout".equals(terminationTrigger)){
                String timeoutVal = getOpt(sm, "timeoutSeconds");
                long secs = Long.parseLong(timeoutVal);
                ScheduledFuture<?> task = scheduler.schedule(() -> cleanupContainer(containerId), secs, TimeUnit.SECONDS);
                timeoutTasks.put(containerId, task);
            } else if ("onHealthCheckFail".equals(terminationTrigger)) { //Schedule Healthcheck monitor
                ScheduledFuture<?> prev = timeoutTasks.remove(containerId);
                if(prev!=null)prev.cancel(false);

                String healthCheckInterval = getOpt(sm, "HealthCheckInterval");
                int intervalSecs = 30;      //Default interval 30 seconds
                if(healthCheckInterval != null){
                    intervalSecs = Integer.parseInt(healthCheckInterval);
                }
                ScheduledFuture<?> healthMonitor = scheduler.scheduleAtFixedRate(() -> {
                    HealthStatus status = getContainerHealth(containerId);
                    if(status == HealthStatus.UNHEALTHY){
                        System.out.print("Terminating unhealthy container: "+containerId+"\n");
                        cleanupContainer(containerId);
                    }
                }, intervalSecs, intervalSecs, TimeUnit.SECONDS);

                timeoutTasks.put(containerId, healthMonitor);
            }
        } catch (Exception e) {
            if (e instanceof NotModifiedException) {
                // If we get a NotModifiedException here, it means the container is already running
                System.out.println("Container already in desired state for " + aasIdShort + "/" + dockerContextName);
            } else {
                System.out.println("Error with Docker context: " + dockerContextName);
                throw new AASOperationException("Error starting Docker container: " + e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    public void terminateRunningContainers(){
        for(String containerId : runningContainers){
            try {
                // Check if container exists before trying to stop it
                boolean containerExists = !dockerClient.listContainersCmd()
                        .withIdFilter(List.of(containerId))
                        .withShowAll(true)
                        .exec()
                        .isEmpty();
                
                if (containerExists) {
                    try {
                        dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
                    } catch (NotModifiedException e) {
                        // Container already stopped, which is fine
                        System.out.println("Container already stopped: " + containerId);
                    } catch (Exception e) {
                        System.err.println("Error stopping container " + containerId + ": " + e.getMessage());
                    }
                    
                    try {
                        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    } catch (Exception e) {
                        System.err.println("Error removing container " + containerId + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("Container no longer exists: " + containerId);
                }
            } catch (Exception e) {
                System.err.println("Error processing container " + containerId + ": " + e.getMessage());
            }
        }
        runningContainers.clear();
    }

    private void cleanupContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception ignored) {}
        runningContainers.remove(containerId);
        ScheduledFuture<?> task = timeoutTasks.remove(containerId);
        if (task != null) task.cancel(false);
    }


//    private Submodel getCounterSubmodel(AASManager aasManager, String aasId) {
//        AssetAdministrationShell aas = aasManager.getAAS(aasId);
//        String smId = aas.getSubmodels().stream()
//                .map(ref -> ref.getKeys().getFirst().getValue())
//                .filter(id -> id.contains(COUNTER_SUBMODEL_ID))
//                .findFirst()
//                .orElse(null);
//
//        return smId != null ? aasManager.getSubmodel(smId) : null;
//
//    }

    //Fetches the Docker context from the given URL and stores it in the repo if it doesn't already exist.
    private Path fetchDockerFileFromURL(URL url, String aasIdShort) throws IOException {
        String contextName = Paths.get(url.getPath()).getFileName().toString();

        //local cache folders for URLs
        Path cacheFolder = Paths.get("/dockerfiles/url-cache");
        Path target = cacheFolder.resolve(contextName);

        //Download the file if it doesn't exist yet
        if (Files.notExists(target)) {
            Files.createDirectories(cacheFolder);
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new AASOperationException("Failed to download Docker context from URL: " + url.toExternalForm(), e);
            }

            //Register the downloaded file in the repository
            dockerRepository.registerDockerFileForAasId(aasIdShort, contextName, target);
        }

        return target;
    }

    ////////////Health Check/////////////////////////
    private enum HealthStatus { HEALTHY, UNHEALTHY, UNKNOWN }

    private HealthStatus getContainerHealth(String containerId) {
        try {
            var inspect = dockerClient.inspectContainerCmd(containerId).exec();
            var state = inspect.getState();
            if(state.getHealth() == null){
                return HealthStatus.UNKNOWN;
            }

            return switch (state.getHealth().getStatus()) {
                case "healthy" -> HealthStatus.HEALTHY;
                case "unhealthy" -> HealthStatus.UNHEALTHY;
                default -> HealthStatus.UNKNOWN;
            };

        } catch (Exception e) {
            return HealthStatus.UNKNOWN;
        }
    }


    //Running Docker-Compose files
    public void runDockerCompose(String aasIdShort, Path dockerComposePath, boolean detach) {
        List<String> command = new ArrayList<>(List.of("docker-compose", "-f", dockerComposePath.toString(), "up"));
        if(detach){command.add("-d");}

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        try {
            Process process = pb.start();
            int exit = process.waitFor();

            if (exit != 0) {
                throw new AASOperationException("Docker compose exited with non-zero status code: " + exit);
            } else {
                runningContainers.add(aasIdShort);
            }
        }catch(IOException | InterruptedException e){
            throw new AASOperationException("Error executing Docker Compose command.", e);
        }
    }

}
