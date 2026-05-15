package be.pxl.researchproject.controller.dto;

public record OfflineScanDTO(
        String localId,
        String studentEmail,
        String bedrijfEmail,
        String scannedAt
) {}
