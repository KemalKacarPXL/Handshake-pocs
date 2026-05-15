package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.StudentCvDownloadResponse;
import be.pxl.researchproject.controller.dto.StudentProfileResponse;
import be.pxl.researchproject.controller.dto.StudentProfileUpdateRequest;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.service.BedrijfService;
import be.pxl.researchproject.service.StudentCvService;
import be.pxl.researchproject.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.LawOfDemeter")
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private StudentCvService studentCvService;

    @Mock
    private BedrijfService bedrijfService;

    @InjectMocks
    private StudentController studentController;

    @Test
    void downloadCv_ReturnsAttachmentResponseForCompany() {
        byte[] pdfBytes = "%PDF-1.4".getBytes();
        when(studentCvService.downloadCv("emma.janssen@student.pxl.be"))
                .thenReturn(new StudentCvDownloadResponse("emma-cv.pdf", "application/pdf", pdfBytes));

        ResponseEntity<byte[]> response = studentController.downloadCv("emma.janssen@student.pxl.be");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains("emma-cv.pdf");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store, no-cache, must-revalidate, max-age=0");
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        verify(studentCvService, times(1)).downloadCv("emma.janssen@student.pxl.be");
    }

    @Test
    void openCv_ReturnsInlinePdfResponse() {
        byte[] pdfBytes = "%PDF-1.4".getBytes();
        when(studentCvService.downloadCv("emma.janssen@student.pxl.be"))
                .thenReturn(new StudentCvDownloadResponse("emma-cv.pdf", "application/pdf", pdfBytes));

        ResponseEntity<byte[]> response = studentController.openCv("emma.janssen@student.pxl.be");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("inline")
                .contains("emma-cv.pdf");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        verify(studentCvService, times(1)).downloadCv("emma.janssen@student.pxl.be");
    }

    @Test
    void downloadCv_StudentNotFound_ThrowsException() {
        when(studentCvService.downloadCv("unknown@student.pxl.be"))
                .thenThrow(new BadRequestException("Student niet gevonden"));

        BadRequestException ex = org.junit.jupiter.api.Assertions.assertThrows(
                BadRequestException.class,
                () -> studentController.downloadCv("unknown@student.pxl.be")
        );

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
        verify(studentCvService, times(1)).downloadCv("unknown@student.pxl.be");
    }

    @Test
    void updateStudentProfile_ReturnsUpdatedProfile() {
        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest(
                "Emma",
                "Janssen",
                "emma.janssen@student.pxl.be",
                "Toegepaste Informatica"
        );
        StudentProfileResponse updatedProfile = new StudentProfileResponse(
                "Emma",
                "Janssen",
                "emma.janssen@student.pxl.be",
                "Toegepaste Informatica",
                false,
                null,
                null,
                false,
                null
        );

        when(studentService.updateStudentProfile("emma.janssen@student.pxl.be", request))
                .thenReturn(updatedProfile);

        ResponseEntity<StudentProfileResponse> response = studentController
                .updateStudentProfile("emma.janssen@student.pxl.be", request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(updatedProfile);
        verify(studentService, times(1)).updateStudentProfile("emma.janssen@student.pxl.be", request);
    }
}
