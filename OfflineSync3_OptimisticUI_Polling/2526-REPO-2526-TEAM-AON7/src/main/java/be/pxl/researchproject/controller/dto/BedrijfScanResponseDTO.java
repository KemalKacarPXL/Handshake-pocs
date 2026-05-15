package be.pxl.researchproject.controller.dto;

import java.time.LocalDateTime;

public record BedrijfScanResponseDTO(
        String studentFirstname,
        String studentLastname,
        String studentEmail,
        String studentEducation,
        LocalDateTime scannedAt
) {}
