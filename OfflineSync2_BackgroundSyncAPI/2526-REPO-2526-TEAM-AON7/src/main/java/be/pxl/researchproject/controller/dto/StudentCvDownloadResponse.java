package be.pxl.researchproject.controller.dto;

public record StudentCvDownloadResponse(
        String fileName,
        String contentType,
        byte[] fileContent
) {
}
