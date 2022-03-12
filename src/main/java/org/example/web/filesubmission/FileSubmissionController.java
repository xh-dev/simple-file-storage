package org.example.web.filesubmission;

import lombok.Data;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("api/v1/file-submission")
public class FileSubmissionController {
    final
    FileSubmissionRepo repo;

    public FileSubmissionController(FileSubmissionRepo repo) {
        this.repo = repo;
    }


    @Data
    public static class PostDto{
        private String description;

    }

    @PostMapping
    @Transactional
    public FileSubmission post(@RequestBody PostDto postDto){
        FileSubmission submission = FileSubmission.builder()
                .description(postDto.description)
                .size(0L)
                .hash(null)
                .data(null)
                .mediaType(null)
                .build();
        return repo.save(submission);
    }
}
