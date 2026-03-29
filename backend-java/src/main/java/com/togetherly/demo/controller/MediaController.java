package com.togetherly.demo.controller;

import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.data.ErrorMessageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MediaController {

    private final Path uploadDir;

    public MediaController(@Value("${media.upload.dir:uploads}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @AuthenticatedApi
    @SecurityRequirements({
            @SecurityRequirement(name = "jwt"),
            @SecurityRequirement(name = "jwt-in-cookie")
    })
    @RequestMapping(path = "/api/media/upload", method = RequestMethod.POST)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("file is empty"),
                    HttpStatus.BAD_REQUEST);
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("only image and video files are allowed"),
                    HttpStatus.BAD_REQUEST);
        }

        // Limit to 20MB
        if (file.getSize() > 20 * 1024 * 1024) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("file too large (max 20MB)"),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(filename);
            file.transferTo(target.toFile());

            String url = "/api/media/" + filename;
            return ResponseEntity.ok(Map.of("url", url, "filename", filename));
        } catch (IOException e) {
            return new ResponseEntity<>(
                    new ErrorMessageResponse("upload failed"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(path = "/api/media/{filename}", method = RequestMethod.GET)
    public ResponseEntity<?> serve(@PathVariable String filename) {
        try {
            Path file = uploadDir.resolve(filename).normalize();
            if (!file.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(file);
            if (contentType == null) contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
