package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.CompanyInviteRepository;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BedrijfServiceTest {
    @Mock
    private BedrijfRepository bedrijfRepository;
    @Mock
    private HandshakeEventRepository handshakeEventRepository;
    @Mock
    private ScanRepository scanRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CompanyInviteRepository companyInviteRepository;

    @InjectMocks
    private BedrijfService bedrijfService;

    private Bedrijf bedrijf;
    private BedrijfRequest bedrijfRequest;

    @BeforeEach
    void setUp() {
        bedrijf = new Bedrijf();
        bedrijf.setFirstname("Jan");
        bedrijf.setLastname("Janssens");
        bedrijf.setEmail("jan@bedrijf.be");
        bedrijf.setPassword("wachtwoord123");
        bedrijfRequest = new BedrijfRequest("Jan", "Janssens", "jan@bedrijf.be", "wachtwoord123");
    }

    @Test
    void createBedrijf_Success() {
        when(bedrijfRepository.existsByEmail("jan@bedrijf.be")).thenReturn(false);
        bedrijfService.createBedrijf(bedrijfRequest);
        verify(bedrijfRepository, times(1)).save(any(Bedrijf.class));
    }

    @Test
    void createBedrijf_PersistsCompanyNameFromRequest() {
        BedrijfRequest requestWithCompany = new BedrijfRequest(
            "Okan", "Kaya", "aca@gmail.com", "wachtwoord123", "ACA");
        when(bedrijfRepository.existsByEmail("aca@gmail.com")).thenReturn(false);

        bedrijfService.createBedrijf(requestWithCompany);

        verify(bedrijfRepository).save(argThat((Bedrijf b) -> "ACA".equals(b.getCompanyName())));
    }

    @Test
    void createBedrijf_AlreadyExists_ThrowsException() {
        when(bedrijfRepository.existsByEmail("jan@bedrijf.be")).thenReturn(true);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> bedrijfService.createBedrijf(bedrijfRequest));
        assertThat(ex.getMessage()).isEqualTo("Email is al in gebruik");
        verify(bedrijfRepository, never()).save(any(Bedrijf.class));
    }

    @Test
    void updateBedrijfProfile_Success() {
        BedrijfProfileUpdateRequest updateRequest = new BedrijfProfileUpdateRequest();
        updateRequest.setFirstname("Jan");
        updateRequest.setLastname("Janssens");
        updateRequest.setEmail("nieuw@bedrijf.be");
        updateRequest.setDescription("Beschrijving");
        updateRequest.setSector("IT");
        updateRequest.setWebsite("www.bedrijf.be");
        updateRequest.setPhoneNumber("0123456789");
        updateRequest.setLocation("Hasselt");
        updateRequest.setOldEmail("jan@bedrijf.be");

        when(bedrijfRepository.findByEmail("jan@bedrijf.be")).thenReturn(Optional.of(bedrijf));
        BedrijfProfileResponse response = bedrijfService.updateBedrijfProfile(updateRequest);
        assertThat(response.email()).isEqualTo("nieuw@bedrijf.be");
        assertThat(response.firstname()).isEqualTo("Jan");
        assertThat(response.sector()).isEqualTo("IT");
        verify(bedrijfRepository, times(1)).save(any(Bedrijf.class));
    }

    @Test
    void updateBedrijfProfile_NotFound_ThrowsException() {
        BedrijfProfileUpdateRequest updateRequest = new BedrijfProfileUpdateRequest();
        updateRequest.setOldEmail("nietbestaat@bedrijf.be");
        when(bedrijfRepository.findByEmail("nietbestaat@bedrijf.be")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> bedrijfService.updateBedrijfProfile(updateRequest));
    }

    @Test
    void getBedrijvenByEvent_ReturnsBedrijven() {
        bedrijf.setSector("IT");
        when(bedrijfRepository.findByJoinedEvent_Id(1L)).thenReturn(List.of(bedrijf));
        List<BedrijfDTO> result = bedrijfService.getBedrijvenByEvent(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jan@bedrijf.be");
        assertThat(result.get(0).getSector()).isEqualTo("IT");
    }
}
