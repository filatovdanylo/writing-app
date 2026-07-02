package com.calmlywriter.repository;

import com.calmlywriter.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}
