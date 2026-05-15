package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.service.BedrijfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.LawOfDemeter")
class BedrijfControllerTest {
    @Mock
    private BedrijfService bedrijfService;
    @InjectMocks
    private BedrijfController bedrijfController;

    @Test
    void getAllBedrijven_ReturnsList() {
        Bedrijf bedrijf = new Bedrijf();
        bedrijf.setFirstname("Jan");
        bedrijf.setLastname("Janssens");
        bedrijf.setEmail("jan@bedrijf.be");
        BedrijfDTO dto = new BedrijfDTO(bedrijf);
        when(bedrijfService.getAllBedrijven()).thenReturn(List.of(dto));
        ResponseEntity<List<BedrijfDTO>> response = bedrijfController.getAllBedrijven();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getEmail()).isEqualTo("jan@bedrijf.be");
        assertThat(response.getBody().get(0).getFirstname()).isEqualTo("Jan");
    }

    @Test
    void createBedrijf_ReturnsCreated() {
        BedrijfRequest req = new BedrijfRequest("Jan", "Janssens", "jan@bedrijf.be", "wachtwoord123");
        ResponseEntity<Void> response = bedrijfController.createBedrijf(req);
        verify(bedrijfService, times(1)).createBedrijf(req);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void getBedrijfProfile_ReturnsProfile() {
        BedrijfProfileResponse resp = new BedrijfProfileResponse("Jan", "Janssens", "jan@bedrijf.be", "desc", "IT", "site", "0123", "Hasselt", false, null);
        when(bedrijfService.getBedrijfProfileByEmail("jan@bedrijf.be")).thenReturn(resp);
        ResponseEntity<BedrijfProfileResponse> response = bedrijfController.getBedrijfProfile("jan@bedrijf.be");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo("jan@bedrijf.be");
    }

    @Test
    void updateBedrijfProfile_ReturnsUpdatedProfile() {
        BedrijfProfileUpdateRequest req = new BedrijfProfileUpdateRequest();
        BedrijfProfileResponse resp = new BedrijfProfileResponse("Jan", "Janssens", "nieuw@bedrijf.be", "desc", "IT", "site", "0123", "Hasselt", false, null);
        when(bedrijfService.updateBedrijfProfile(req)).thenReturn(resp);
        ResponseEntity<BedrijfProfileResponse> response = bedrijfController.updateBedrijfProfile(req);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo("nieuw@bedrijf.be");
    }

    @Test
    void getBedrijvenByEvent_ReturnsList() {
        var bedrijf = new Bedrijf();
        bedrijf.setFirstname("Jan");
        bedrijf.setLastname("Janssens");
        bedrijf.setEmail("jan@bedrijf.be");
        bedrijf.setSector("IT");
        BedrijfDTO dto = new BedrijfDTO(bedrijf);
        when(bedrijfService.getBedrijvenByEvent(1L)).thenReturn(List.of(dto));
        ResponseEntity<List<BedrijfDTO>> response = bedrijfController.getBedrijvenByEvent(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getEmail()).isEqualTo("jan@bedrijf.be");
        assertThat(response.getBody().get(0).getSector()).isEqualTo("IT");
    }
}
