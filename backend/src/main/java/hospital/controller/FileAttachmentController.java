package hospital.controller;

import hospital.entity.FileAttachment;
import hospital.service.FileAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileAttachmentController {
    private final FileAttachmentService fileAttachmentService;

    @PostMapping("/upload")
    public ResponseEntity<FileAttachment> uploadFile(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("relatedType") String relatedType,
                                                    @RequestParam("relatedId") Long relatedId) {
        FileAttachment att = fileAttachmentService.uploadFile(file, relatedType, relatedId);
        return ResponseEntity.ok(att);
    }

    @GetMapping("/by-relation")
    public ResponseEntity<List<FileAttachment>> getFilesByRelation(@RequestParam String relatedType,
                                                                  @RequestParam Long relatedId) {
        return ResponseEntity.ok(fileAttachmentService.getFilesByRelation(relatedType, relatedId));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        FileAttachment att = fileAttachmentService.getFileById(id);
        byte[] data = fileAttachmentService.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + att.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(att.getFileType()))
                .body(data);
    }
}
