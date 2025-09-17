package org.example.database;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Component
public class DockerRepository {
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    private final Path tempBase = Paths.get("dockerfiles");
    private final Path tempZipDir = Paths.get("temp_zip");

    @Autowired
    public DockerRepository(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
        try {
            Files.createDirectories(tempZipDir);
        } catch (IOException e) {
            System.err.println("Failed to create temp directory: " + e.getMessage());
        }
    }
    
    /**
     * Zips a directory into a temporary file
     * @param sourceDir Directory to zip
     * @return Path to the created zip file
     */
    private Path zipDirectory(Path sourceDir) throws IOException {
        Path zipFile = Files.createTempFile(tempZipDir, "docker", ".zip");
        
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String relativePath = sourceDir.relativize(file).toString().replace('\\', '/');
                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sourceDir)) {
                        String relativePath = sourceDir.relativize(dir).toString().replace('\\', '/') + "/";
                        ZipEntry zipEntry = new ZipEntry(relativePath);
                        zos.putNextEntry(zipEntry);
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        
        return zipFile;
    }
    
    /**
     * Unzips a file to a directory
     * @param zipFile Path to the zip file
     * @param targetDir Directory to extract to
     */
    private void unzipFile(Path zipFile, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path resolvedPath = targetDir.resolve(zipEntry.getName());
                
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    // Create parent directories if they don't exist
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zis, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    // Compute SHA‑256 checksum of a file or directory in a streaming fashion
    private String computeChecksum(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            if (Files.isDirectory(path)) {
                // For directories, compute a combined checksum of all files
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Add the relative path to the digest to ensure folder structure is part of the checksum
                        String relativePath = path.relativize(file).toString();
                        digest.update(relativePath.getBytes());

                        // Add the file content to the digest
                        try (InputStream in = Files.newInputStream(file)) {
                            byte[] buf = new byte[8 * 1024];
                            int len;
                            while ((len = in.read(buf)) != -1) {
                                digest.update(buf, 0, len);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // Original single file checksum logic
                try (InputStream in = Files.newInputStream(path)) {
                    byte[] buf = new byte[8 * 1024];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        digest.update(buf, 0, len);
                    }
                }
            }
            
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not supported", e);
        }
    }

    /**
     * Stores or updates a Docker context (file or directory) in GridFS:
     * - If an identical file/directory (same checksum) already exists → skip
     * - If name/AAS match but checksum differs → delete old and store new
     * 
     * @param aasIdShort The AAS ID short name
     * @param dockerContextName Name of the Docker context (can be a file or directory name)
     * @param dockerContextPath Path to the Docker file or directory
     */
    public void registerDockerFileForAasId(String aasIdShort,
                                           String dockerContextName,
                                           Path dockerContextPath) throws IOException {
        // 1) Compute checksum of the file or directory
        String checksum = computeChecksum(dockerContextPath);
        
        // 2) Check for an exact match (same AAS ID, context name, checksum)
        Query exactMatchQuery = Query.query(
                Criteria.where("metadata.aasIdShort").is(aasIdShort)
                        .and("filename").is(dockerContextName)
        );

        var iterable = gridFsTemplate.find(exactMatchQuery);
        if (iterable.iterator().hasNext()) {
            GridFSFile existing = iterable.iterator().next();
            String oldChecksum = Objects.requireNonNull(existing.getMetadata()).getString("checksum");
            if (oldChecksum.equals(checksum)) {
                System.out.println("This Docker context already exists. Skipping: " + aasIdShort + "/" + dockerContextName);
                // identical context already exists → nothing to do
                return;
            }
            // otherwise: delete old one before storing new
            System.out.println("Docker context exists but checksum differs. Replacing: " + aasIdShort + "/" + dockerContextName);
            gridFsTemplate.delete(exactMatchQuery);
        }

        // 3) Prepare the content to store
        Path contentToStore = dockerContextPath;
        boolean isTemporary = false;
        
        // If it's a directory, zip it first
        if (Files.isDirectory(dockerContextPath)) {
            contentToStore = zipDirectory(dockerContextPath);
            isTemporary = true;
        }
        
        // 4) Store the new version with its metadata
        try (InputStream in = Files.newInputStream(contentToStore)) {
            DBObject metadata = new BasicDBObject();
            metadata.put("aasIdShort", aasIdShort);
            metadata.put("checksum", checksum);
            metadata.put("isDirectory", Files.isDirectory(dockerContextPath));
            gridFsTemplate.store(in, dockerContextName, metadata);
        } finally {
            // Clean up temporary zip file if created
            if (isTemporary) {
                // Force garbage collection to release file handles
                System.gc();
                safeDeleteFile(contentToStore);
            }
        }
        
        System.out.println("Registered: " + aasIdShort + "/" + dockerContextName + 
                          (Files.isDirectory(dockerContextPath) ? " (directory)" : " (file)"));
    }



    /**
     * Safely delete a file with retries
     * @param file Path to the file to delete
     */
    private void safeDeleteFile(Path file) {
        if (!Files.exists(file)) {
            return;
        }
        
        // Try up to 3 times with increasing delays
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Files.deleteIfExists(file);
                return; // Success
            } catch (IOException e) {
                if (attempt < 3) {
                    System.err.println("Failed to delete file (attempt " + attempt + "): " + file + 
                                      ". Will retry after delay. Error: " + e.getMessage());
                    try {
                        // Increase delay with each attempt
                        Thread.sleep(attempt * 500L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("Failed to delete file after " + attempt + " attempts: " + file + 
                                      ". File will be cleaned up on JVM exit. Error: " + e.getMessage());
                    // As a last resort, try to mark for deletion on JVM exit
                    file.toFile().deleteOnExit();
                }
            }
        }
    }

    /**
     * Retrieve a Docker context (file or directory), caching locally under dockerfiles/{aasIdShort}/
     * 
     * @param aasIdShort The AAS ID short name
     * @param dockerContextName Name of the Docker context (file or directory)
     * @return Path to the retrieved file or directory
     */
    public Path getDockerFile(String aasIdShort, String dockerContextName) throws IOException {
        Path dir = tempBase.resolve(aasIdShort);
        Path localPath = dir.resolve(dockerContextName);

        // 1) Look up the GridFSFile, so we can read its metadata
        Query q = Query.query(
                Criteria.where("metadata.aasIdShort").is(aasIdShort)
                        .and("filename").is(dockerContextName)
        )
                .with(Sort.by(Sort.Direction.DESC, "uploadDate"));

        var iterable = gridFsTemplate.find(q);
        if (!iterable.iterator().hasNext()) {
            System.out.println("WARNING: Docker context not found in GridFS: " + aasIdShort + "/" + dockerContextName);
            return null;
        }
        GridFSFile gridFsFile = gridFsTemplate.findOne(q);
        
        // Get metadata
        var metadata = Objects.requireNonNull(gridFsFile.getMetadata());
        String remoteChecksum = (String) metadata.get("checksum");
        boolean isDirectory = metadata.containsKey("isDirectory") && (boolean) metadata.get("isDirectory");

        // 2) If the file/directory exists locally, compare checksums
        if (Files.exists(localPath)) {
            try {
                String localChecksum = computeChecksum(localPath);
                if (remoteChecksum.equals(localChecksum)) {
                    // still up to date!
                    System.out.println("Using cached " + (isDirectory ? "directory" : "file") + 
                                      ": " + aasIdShort + "/" + dockerContextName);
                    return localPath;
                } else {
                    // stale on disk → delete it so we re-download below
                    System.out.println("Local " + (isDirectory ? "directory" : "file") + 
                                      " is stale, re-downloading: " + aasIdShort + "/" + dockerContextName);
                    if (isDirectory) {
                        // Delete directory recursively
                        try {
                            Files.walk(localPath)
                                 .sorted(Comparator.reverseOrder())
                                 .forEach(path -> {
                                     try {
                                         Files.deleteIfExists(path);
                                     } catch (IOException e) {
                                         System.err.println("Failed to delete: " + path);
                                     }
                                 });
                        } catch (IOException e) {
                            System.err.println("Error deleting directory: " + e.getMessage());
                            // Continue anyway - we'll overwrite files
                        }
                    } else {
                        try {
                            Files.delete(localPath);
                        } catch (IOException e) {
                            System.err.println("Error deleting file: " + e.getMessage());
                            // Continue anyway - we'll overwrite the file
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error checking local checksum, will re-download: " + e.getMessage());
                // Continue to re-download
            }
        }

        // 3) (re)create the dir & stream down the fresh copy
        Files.createDirectories(dir);
        
        if (isDirectory) {
            // For directories: download to a temp zip file, then extract
            Path tempZipFile = null;
            try {
                tempZipFile = Files.createTempFile(tempZipDir, "docker", ".zip");
                
                // Download the zip file
                try (InputStream in = gridFsOperations.getResource(gridFsFile).getInputStream()) {
                    Files.copy(in, tempZipFile, StandardCopyOption.REPLACE_EXISTING);
                }
                
                // Extract the zip file to the target directory
                Files.createDirectories(localPath);
                unzipFile(tempZipFile, localPath);
                System.out.println("Downloaded and extracted directory: " + aasIdShort + "/" + dockerContextName);
                
                // Force garbage collection to release file handles
                System.gc();
                
            } finally {
                // Clean up the temporary zip file with safe delete
                if (tempZipFile != null) {
                    safeDeleteFile(tempZipFile);
                }
            }
        } else {
            // For single files: direct download
            try (InputStream in = gridFsOperations.getResource(gridFsFile).getInputStream()) {
                Files.copy(in, localPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Downloaded file: " + aasIdShort + "/" + dockerContextName);
            }
        }
        
        return localPath;
    }

    /**
     * Delete a specific Docker context entry from GridFS.
     * 
     * @param aasIdShort The AAS ID short name
     * @param dockerContextName Name of the Docker context to delete
     */
    public void unregisterDockerFile(String aasIdShort, String dockerContextName) {
        Query q = Query.query(
                Criteria.where("metadata.aasIdShort").is(aasIdShort)
                        .and("filename").is(dockerContextName)
        );
        gridFsTemplate.delete(q);
        System.out.println("Unregistered Docker context: " + aasIdShort + "/" + dockerContextName);
        
        // Also clean up local cache if it exists
        Path localPath = tempBase.resolve(aasIdShort).resolve(dockerContextName);
        if (Files.exists(localPath)) {
            // Force garbage collection to release file handles
            System.gc();

            if (Files.isDirectory(localPath)) {
                try {
                    Files.walk(localPath)
                         .sorted(Comparator.reverseOrder())
                         .forEach(path -> {
                             try {
                                 Files.deleteIfExists(path);
                             } catch (IOException e) {
                                 System.err.println("Failed to delete: " + path);
                                 path.toFile().deleteOnExit();
                             }
                         });
                } catch (IOException e) {
                    System.err.println("Error walking directory for deletion: " + e.getMessage());
                }
            } else {
                safeDeleteFile(localPath);
            }
            System.out.println("Cleaned up local cache for: " + aasIdShort + "/" + dockerContextName);
        }
    }

    /**
     * Clean up local cache on shutdown.
     */
    @PreDestroy
    public void destroy() {
        // Force garbage collection to release file handles
        System.gc();
        
        // Clean up main docker files directory
        if (Files.exists(tempBase)) {
            try {
                Files.walk(tempBase)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { 
                                Files.deleteIfExists(path); 
                            } catch (IOException e) {
                                System.err.println("Failed to delete on shutdown: " + path);
                                path.toFile().deleteOnExit();
                            }
                        });
            } catch (IOException e) {
                System.err.println("Error cleaning up docker files directory: " + e.getMessage());
            }
        }
        
        // Clean up temporary zip directory
        if (Files.exists(tempZipDir)) {
            try {
                Files.walk(tempZipDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { 
                                Files.deleteIfExists(path); 
                            } catch (IOException e) {
                                System.err.println("Failed to delete on shutdown: " + path);
                                path.toFile().deleteOnExit();
                            }
                        });
            } catch (IOException e) {
                System.err.println("Error cleaning up temp zip directory: " + e.getMessage());
            }
        }
    }
}