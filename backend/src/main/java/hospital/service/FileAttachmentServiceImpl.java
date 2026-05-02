package hospital.service;

import hospital.entity.FileAttachment;
import hospital.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAttachmentServiceImpl implements FileAttachmentService {
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("application/pdf", "image/jpeg", "image/png");

    @Override
    public FileAttachment uploadFile(MultipartFile file, String relatedType, Long relatedId) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        if (file.getSize() > MAX_FILE_SIZE) throw new RuntimeException("File too large (max 5MB)");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new RuntimeException("Invalid file type");
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String ext = getExtension(file.getOriginalFilename());
            String uniqueName = UUID.randomUUID() + (ext.isEmpty() ? "" : ("." + ext));
            Path filePath = Paths.get(uploadDir, uniqueName);
            Files.copy(file.getInputStream(), filePath);
            FileAttachment att = new FileAttachment();
            att.setFileName(file.getOriginalFilename());
            att.setFileUrl("/api/files/download/" + uniqueName);
            att.setFileType(file.getContentType());
            att.setRelatedType(relatedType);
            att.setRelatedId(relatedId);
            att.setUploadedAt(LocalDateTime.now());
            fileAttachmentRepository.save(att);
            return att;
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public List<FileAttachment> getFilesByRelation(String relatedType, Long relatedId) {
        return fileAttachmentRepository.findByRelatedTypeAndRelatedId(relatedType, relatedId);
    }

    @Override
    public FileAttachment getFileById(Long id) {
        return fileAttachmentRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    @Override
    public byte[] downloadFile(Long id) {
        FileAttachment att = getFileById(id);
        String fileName = att.getFileUrl().replace("/api/files/download/", "");
        Path filePath = Paths.get(uploadDir, fileName);
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("File not found on disk", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx == -1 ? "" : filename.substring(idx + 1);
    }
}
