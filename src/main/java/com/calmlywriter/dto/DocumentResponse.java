package com.calmlywriter.dto;

import com.calmlywriter.entity.Document;
import java.time.Instant;

public record DocumentResponse(Long id, String title, String content, Instant createdAt, Instant updatedAt) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public static DocumentResponse summary(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                null,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
