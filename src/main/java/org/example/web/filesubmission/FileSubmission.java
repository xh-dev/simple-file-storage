package org.example.web.filesubmission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class FileSubmission {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false, length = 16)
    private UUID id;
    private Long size;
    private String mediaType;
    private String description;
    private String hash;
    @Lob
    private byte[] data;
    private Date lastModified;
}
