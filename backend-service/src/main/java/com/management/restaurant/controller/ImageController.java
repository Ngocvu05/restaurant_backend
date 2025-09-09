package com.management.restaurant.controller;

import com.management.restaurant.dto.ImageDTO;
import com.management.restaurant.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @GetMapping
    public ResponseEntity<List<ImageDTO>> getAll() {
        return ResponseEntity.ok(imageService.getAllImages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImageById(id));
    }

    @PostMapping
    public ResponseEntity<ImageDTO> create(@RequestBody ImageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imageService.createImage(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageDTO> update(@PathVariable Long id, @RequestBody ImageDTO dto) {
        return ResponseEntity.ok(imageService.updateImage(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}