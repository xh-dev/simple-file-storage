package org.example.web.filesubmission;

import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/file-submission/{id}")
public class FileSubmissionByIdController {
    final
    FileSubmissionRepo repo;

    public FileSubmissionByIdController(FileSubmissionRepo repo) {
        this.repo = repo;
    }

    @Autowired
    HttpServletResponse response;

    @Data
    public static class PutRequest {
        private MultipartFile file;
        private String fileType;
    }

    @Data
    @Builder
    public static class PutResponse {
        private UUID id;
        private String description;
        private Long size;
        private String hash;
        private String mediaType;
        private Date lastModified;

    }

    @DeleteMapping
    @Transactional
    public PutResponse delete(@PathVariable UUID id){
        return Try
                .of(()->repo.findById(id).get())
                .map(it->{
                    repo.delete(it);
                    return PutResponse.builder()
                            .id(it.getId())
                            .mediaType(it.getMediaType())
                            .hash(it.getHash())
                            .size(it.getSize())
                            .description(it.getDescription())
                            .lastModified(it.getLastModified())
                            .build();
                }).get();
    }

    @PutMapping
    @Transactional
    public PutResponse put(
            @PathVariable UUID id,
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam("fileType") String fileType) {
        return Try
                .of(() -> repo.findById(id).get())
                .filter(it -> !file.isEmpty() && fileType != null)
                .filter(it -> Arrays.asList("png", "jpeg", "gif", "binary").contains(fileType))
                .mapTry(fileSubmission -> {
                    fileSubmission.setSize(file.getSize());
                    fileSubmission.setData(file.getBytes());
                    switch (fileType) {
                        case "png":
                            fileSubmission.setMediaType(MediaType.IMAGE_PNG_VALUE);
                            break;
                        case "jpeg":
                            fileSubmission.setMediaType(MediaType.IMAGE_JPEG_VALUE);
                            break;
                        case "gif":
                            fileSubmission.setMediaType(MediaType.IMAGE_GIF_VALUE);
                            break;
                        case "binary":
                            fileSubmission.setMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                            break;
                    }
                    byte[] digest = MessageDigest.getInstance("SHA-256").digest(fileSubmission.getData());
                    String hash = Hex.encodeHexString(digest);
                    fileSubmission.setHash(hash);
                    fileSubmission.setLastModified(new Date());
                    return fileSubmission;
                })
                .map(fileSubmission ->
                        PutResponse
                                .builder()
                                .hash(fileSubmission.getHash())
                                .size(fileSubmission.getSize())
                                .id(fileSubmission.getId())
                                .description(fileSubmission.getDescription())
                                .lastModified(fileSubmission.getLastModified())
                                .build()
                ).get();
    }

    @GetMapping
    @Transactional
    public void get(@PathVariable UUID id) {
        Try
                .of(() -> repo.findById(id).get())
                .filter(it -> it.getData() != null)
                .map(it -> {
                    if (MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(it.getMediaType())) {
                        return new Tuple2<>(MediaType.APPLICATION_OCTET_STREAM, it);
                    } else if (MediaType.IMAGE_GIF_VALUE.equals(it.getMediaType())) {
                        return new Tuple2<>(MediaType.IMAGE_GIF, it);
                    } else if (MediaType.IMAGE_JPEG_VALUE.equals(it.getMediaType())) {
                        return new Tuple2<>(MediaType.IMAGE_JPEG, it);
                    } else if (MediaType.IMAGE_PNG_VALUE.equals(it.getMediaType())) {
                        return new Tuple2<>(MediaType.IMAGE_PNG, it);
                    } else {
                        throw new RuntimeException("No expected media type");
                    }
                })
                .mapTry(it -> {
                    MediaType mediaType = it._1();
                    FileSubmission fileSubmission = it._2();
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(mediaType.getType());
                    response.setContentLength(Math.toIntExact(fileSubmission.getSize()));
                    IOUtils.copy(new ByteArrayInputStream(fileSubmission.getData()), response.getOutputStream());
                    return it;
                }).get();
    }
}
