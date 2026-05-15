package be.pxl.researchproject.controller.dto;

import java.time.LocalDateTime;

public record StudentScanResponseDTO(
        String bedrijfName,
        String bedrijfEmail,
        String bedrijfSector,
        LocalDateTime scannedAt
) {}