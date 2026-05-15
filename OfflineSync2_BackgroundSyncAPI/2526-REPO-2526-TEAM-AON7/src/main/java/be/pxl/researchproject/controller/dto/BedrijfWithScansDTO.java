package be.pxl.researchproject.controller.dto;

import java.util.List;

public record BedrijfWithScansDTO(
        Long id,
        String companyName,
        String sector,
        String email,
        int scanCount,
        List<BedrijfScanResponseDTO> scans
) {}
