package com.calmlywriter.controller;

import com.calmlywriter.dto.DocumentRequest;
import com.calmlywriter.dto.DocumentResponse;
import com.calmlywriter.security.UserPrincipal;
import com.calmlywriter.service.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentResponse> list(Authentication authentication) {
        return documentService.listForUser(currentUserId(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(Authentication authentication) {
        return documentService.create(currentUserId(authentication));
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable Long id, Authentication authentication) {
        return documentService.getById(currentUserId(authentication), id);
    }

    @PutMapping("/{id}")
    public DocumentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request,
            Authentication authentication
    ) {
        return documentService.update(currentUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        documentService.delete(currentUserId(authentication), id);
    }

    private Long currentUserId(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).userId();
    }
}
