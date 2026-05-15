package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.StudentCvDownloadResponse;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class StudentCvService {
    private static final long MAX_CV_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final StudentRepository studentRepository;

    @Autowired
    public StudentCvService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentCvDownloadResponse downloadCv(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        StudentRepository.StudentCvProjection cv = studentRepository.findCvByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Student niet gevonden"));

        byte[] fileContent = cv.getCvFileContent();
        if (fileContent == null || fileContent.length == 0) {
            throw new BadRequestException("Deze student heeft nog geen CV geüpload");
        }

        String fileName = cv.getCvFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "cv.pdf";
        }

        String contentType = cv.getCvContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/pdf";
        }

        return new StudentCvDownloadResponse(fileName, contentType, fileContent);
    }

    @Transactional
    public void uploadCv(String email, MultipartFile file) {
        validateCvFile(file);
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        String originalFilename = resolveOriginalFilename(file);

        try {
            int updatedRows = studentRepository.updateCvByEmail(
                    normalizedEmail,
                    originalFilename,
                    "application/pdf",
                    file.getSize(),
                    file.getBytes()
            );
            if (updatedRows == 0) {
                throw new BadRequestException("Student niet gevonden");
            }
        } catch (IOException ex) {
            throw new BadRequestException("CV kon niet verwerkt worden");
        }
    }

    @Transactional
    public void deleteCv(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        int updatedRows = studentRepository.deleteCvByEmail(normalizedEmail);
        if (updatedRows == 0) {
            throw new BadRequestException("Student niet gevonden");
        }
    }

    private boolean isPdfFile(MultipartFile file, String originalFilename) {
        boolean hasPdfExtension = originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean hasPdfMimeType = "application/pdf".equalsIgnoreCase(file.getContentType());

        if (hasPdfExtension || hasPdfMimeType) {
            return true;
        }

        try {
            byte[] bytes = file.getBytes();
            return bytes.length >= 4
                    && bytes[0] == 0x25
                    && bytes[1] == 0x50
                    && bytes[2] == 0x44
                    && bytes[3] == 0x46;
        } catch (IOException ex) {
            return false;
        }
    }

    private void validateCvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CV-bestand ontbreekt");
        }

        if (file.getSize() > MAX_CV_FILE_SIZE_BYTES) {
            throw new BadRequestException("CV-bestand is te groot. Maximum 10 MB.");
        }

        String originalFilename = resolveOriginalFilename(file);
        if (!isPdfFile(file, originalFilename)) {
            throw new BadRequestException("Alleen PDF-bestanden zijn toegestaan");
        }
    }

    private String resolveOriginalFilename(MultipartFile file) {
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            return "cv.pdf";
        }
        return file.getOriginalFilename().trim();
    }
}
