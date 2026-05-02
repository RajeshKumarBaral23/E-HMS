package hospital.service;

import hospital.entity.FileAttachment;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileAttachmentService {
    FileAttachment uploadFile(MultipartFile file, String relatedType, Long relatedId);
    List<FileAttachment> getFilesByRelation(String relatedType, Long relatedId);
    FileAttachment getFileById(Long id);
    byte[] downloadFile(Long id);
}
