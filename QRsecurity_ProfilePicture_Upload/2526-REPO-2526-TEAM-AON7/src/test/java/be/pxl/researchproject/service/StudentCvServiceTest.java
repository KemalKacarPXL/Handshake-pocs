package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.StudentCvDownloadResponse;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentCvServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentCvService studentCvService;

    @Test
    void uploadCv_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "%PDF-1.4".getBytes());

        when(studentRepository.updateCvByEmail(eq("john@student.pxl.be"), eq("cv.pdf"), eq("application/pdf"), anyLong(), any()))
                .thenReturn(1);

        studentCvService.uploadCv("john@student.pxl.be", file);

        verify(studentRepository, times(1)).updateCvByEmail(anyString(), anyString(), anyString(), anyLong(), any());
    }

    @Test
    void uploadCv_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.exe", "application/octet-stream", "evil-bytes".getBytes());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentCvService.uploadCv("john@student.pxl.be", file));

        assertThat(ex.getMessage()).isEqualTo("Alleen PDF-bestanden zijn toegestaan");
    }

    @Test
    void uploadCv_StudentNotFound_ThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "%PDF-1.4".getBytes());

        when(studentRepository.updateCvByEmail(anyString(), anyString(), anyString(), anyLong(), any())).thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentCvService.uploadCv("unknown@student.pxl.be", file));

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
    }

    @Test
    void downloadCv_Success() {
        byte[] pdfBytes = "%PDF-1.4".getBytes();
        StudentRepository.StudentCvProjection projection = mock(StudentRepository.StudentCvProjection.class);

        when(projection.getCvFileContent()).thenReturn(pdfBytes);
        when(projection.getCvContentType()).thenReturn("application/pdf");
        when(projection.getCvFileName()).thenReturn("cv.pdf");
        when(studentRepository.findCvByEmail("john@student.pxl.be")).thenReturn(Optional.of(projection));

        StudentCvDownloadResponse response = studentCvService.downloadCv("john@student.pxl.be");

        assertThat(response.fileName()).isEqualTo("cv.pdf");
        assertThat(response.contentType()).isEqualTo("application/pdf");
        assertThat(response.fileContent()).isEqualTo(pdfBytes);
    }

    @Test
    void downloadCv_NoCv_ThrowsException() {
        StudentRepository.StudentCvProjection projection = mock(StudentRepository.StudentCvProjection.class);

        when(projection.getCvFileContent()).thenReturn(null);
        when(studentRepository.findCvByEmail("john@student.pxl.be")).thenReturn(Optional.of(projection));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentCvService.downloadCv("john@student.pxl.be"));

        assertThat(ex.getMessage()).isEqualTo("Deze student heeft nog geen CV geüpload");
    }

    @Test
    void downloadCv_StudentNotFound_ThrowsException() {
        when(studentRepository.findCvByEmail("unknown@student.pxl.be")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentCvService.downloadCv("unknown@student.pxl.be"));

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
    }

    @Test
    void deleteCv_Success() {
        when(studentRepository.deleteCvByEmail("john@student.pxl.be")).thenReturn(1);

        studentCvService.deleteCv("john@student.pxl.be");

        verify(studentRepository, times(1)).deleteCvByEmail("john@student.pxl.be");
    }

    @Test
    void deleteCv_StudentNotFound_ThrowsException() {
        when(studentRepository.deleteCvByEmail("unknown@pxl.be")).thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentCvService.deleteCv("unknown@pxl.be"));

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
    }
}
