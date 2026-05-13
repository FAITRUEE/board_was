package com.example.board.dto.response;

import com.example.board.entity.PostAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String originalFileName;
    private String storedFileName;
    private String filePath;           // ✅ 추가
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;

    public static AttachmentResponse fromEntity(PostAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .storedFileName(attachment.getStoredFileName())
                .filePath(attachment.getFilePath())  // ✅ 추가
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .uploadedAt(attachment.getCreatedAt())
                .build();
    }
}