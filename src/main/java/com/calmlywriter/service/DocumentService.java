package com.calmlywriter.service;

import com.calmlywriter.dto.DocumentRequest;
import com.calmlywriter.dto.DocumentResponse;
import com.calmlywriter.entity.Document;
import com.calmlywriter.entity.User;
import com.calmlywriter.exception.NotFoundException;
import com.calmlywriter.repository.DocumentRepository;
import com.calmlywriter.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listForUser(Long userId) {
        return documentRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(DocumentResponse::summary)
                .toList();
    }

    @Transactional
    public DocumentResponse create(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Document document = documentRepository.save(new Document(user));
        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(Long userId, Long documentId) {
        Document document = findOwnedDocument(userId, documentId);
        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse update(Long userId, Long documentId, DocumentRequest request) {
        Document document = findOwnedDocument(userId, documentId);
        document.setTitle(request.title());
        document.setContent(request.content() != null ? request.content() : "");
        return DocumentResponse.from(document);
    }

    @Transactional
    public void delete(Long userId, Long documentId) {
        Document document = findOwnedDocument(userId, documentId);
        documentRepository.delete(document);
    }

    private Document findOwnedDocument(Long userId, Long documentId) {
        return documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }
}
