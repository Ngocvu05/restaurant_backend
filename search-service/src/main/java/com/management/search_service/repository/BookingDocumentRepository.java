package com.management.search_service.repository;

import com.management.search_service.document.BookingDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDocumentRepository extends ElasticsearchRepository<BookingDocument, String> {
    Optional<BookingDocument> findByBookingId(Long bookingId);

    List<BookingDocument> findByUserId(Long userId);

    Page<BookingDocument> findByStatus(String status, Pageable pageable);
}