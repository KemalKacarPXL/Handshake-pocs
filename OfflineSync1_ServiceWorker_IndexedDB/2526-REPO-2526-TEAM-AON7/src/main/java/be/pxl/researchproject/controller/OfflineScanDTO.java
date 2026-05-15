package be.pxl.researchproject.controller;

import java.time.LocalDateTime;

public record OfflineScanDTO(
        String localId,       // UUID gegenereerd op de client
        String studentEmail,
        String bedrijfEmail,
        LocalDateTime scannedAt,
        boolean alreadySynced
) {}
