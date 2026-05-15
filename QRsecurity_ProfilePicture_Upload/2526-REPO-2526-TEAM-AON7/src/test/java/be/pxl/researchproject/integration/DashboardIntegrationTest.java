package be.pxl.researchproject.integration;

import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BedrijfRepository bedrijfRepository;

    @Autowired
    private HandshakeEventRepository handshakeEventRepository;

    @Autowired
    private ScanRepository scanRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        scanRepository.deleteAll();
        studentRepository.deleteAll();
        bedrijfRepository.deleteAll();
        handshakeEventRepository.deleteAll();
    }

    private HandshakeEvent savedEvent(String title) {
        HandshakeEvent e = new HandshakeEvent(title, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0), "Hasselt");
        return handshakeEventRepository.save(e);
    }

    private Bedrijf savedBedrijf(String companyName, String sector, String email, HandshakeEvent event) {
        Bedrijf b = new Bedrijf();
        b.setFirstname("Test");
        b.setLastname("Bedrijf");
        b.setEmail(email);
        b.setCompanyName(companyName);
        b.setSector(sector);
        b.setJoinedEvent(event);
        return bedrijfRepository.save(b);
    }

    private Scan savedScan(Bedrijf bedrijf, String firstName, String lastName, String email, String education) {
        Scan scan = new Scan();
        scan.setStudentFirstname(firstName);
        scan.setStudentLastname(lastName);
        scan.setStudentEmail(email);
        scan.setStudentEducation(education);
        scan.setScannedAt(LocalDateTime.now());
        scan.setBedrijf(bedrijf);
        return scanRepository.save(scan);
    }

    // --- GET /api/bedrijf/by-event/{eventId}/with-scans ---

    @Test
    void getBedrijvenWithScans_returnsEmptyList_whenNoCompaniesInEvent() throws Exception {
        HandshakeEvent event = savedEvent("Leeg Event");

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getBedrijvenWithScans_returnsOnlyBedrijvenInSpecifiedEvent() throws Exception {
        HandshakeEvent event1 = savedEvent("Event 1");
        HandshakeEvent event2 = savedEvent("Event 2");

        savedBedrijf("Cegeka", "IT", "cegeka@test.be", event1);
        savedBedrijf("Mobly",  "E-commerce", "mobly@test.be", event2);

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Cegeka"));
    }

    @Test
    void getBedrijvenWithScans_returnsBedrijfWithZeroScans_whenNoScansExist() throws Exception {
        HandshakeEvent event = savedEvent("Event");
        savedBedrijf("Cegeka", "IT", "cegeka@test.be", event);

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Cegeka"))
                .andExpect(jsonPath("$[0].scanCount").value(0))
                .andExpect(jsonPath("$[0].scans").isEmpty());
    }

    @Test
    void getBedrijvenWithScans_returnsCorrectScanCountAndStudentData() throws Exception {
        HandshakeEvent event = savedEvent("Event");
        Bedrijf bedrijf = savedBedrijf("Cegeka", "Software Development", "cegeka@test.be", event);

        savedScan(bedrijf, "Arda", "Güler", "arda@student.be", "Professionele Bachelor TI");
        savedScan(bedrijf, "Emma", "Janssen", "emma@student.be", "Professionele Bachelor TI");

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scanCount").value(2))
                .andExpect(jsonPath("$[0].scans.length()").value(2))
                .andExpect(jsonPath("$[0].scans[0].studentFirstname").value("Arda"))
                .andExpect(jsonPath("$[0].scans[0].studentLastname").value("Güler"))
                .andExpect(jsonPath("$[0].scans[0].studentEmail").value("arda@student.be"))
                .andExpect(jsonPath("$[0].scans[0].studentEducation").value("Professionele Bachelor TI"))
                .andExpect(jsonPath("$[0].scans[1].studentFirstname").value("Emma"));
    }

    @Test
    void getBedrijvenWithScans_returnsBedrijfMetadata() throws Exception {
        HandshakeEvent event = savedEvent("Event");
        savedBedrijf("Cegeka", "Software Development", "cegeka@test.be", event);

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Cegeka"))
                .andExpect(jsonPath("$[0].sector").value("Software Development"))
                .andExpect(jsonPath("$[0].email").value("cegeka@test.be"))
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    void getBedrijvenWithScans_scansAreIsolatedPerBedrijf() throws Exception {
        HandshakeEvent event = savedEvent("Event");
        Bedrijf cegeka = savedBedrijf("Cegeka", "IT", "cegeka@test.be", event);
        Bedrijf mobly  = savedBedrijf("Mobly",  "E-commerce", "mobly@test.be", event);

        savedScan(cegeka, "Arda",  "Güler",   "arda@student.be",  "Bachelor TI");
        savedScan(cegeka, "Emma",  "Janssen",  "emma@student.be",  "Bachelor TI");
        savedScan(mobly,  "Lotte", "Mertens",  "lotte@student.be", "Bachelor TI");

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Cegeka heeft 2 scans
        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.companyName == 'Cegeka')].scanCount").value(2))
                .andExpect(jsonPath("$[?(@.companyName == 'Mobly')].scanCount").value(1));
    }

    @Test
    void getBedrijvenWithScans_scansFromOtherEventBedrijfAreNotIncluded() throws Exception {
        HandshakeEvent event1 = savedEvent("Event 1");
        HandshakeEvent event2 = savedEvent("Event 2");

        Bedrijf bedrijf1 = savedBedrijf("Cegeka", "IT", "cegeka@test.be", event1);
        Bedrijf bedrijf2 = savedBedrijf("Mobly",  "E-commerce", "mobly@test.be", event2);

        savedScan(bedrijf1, "Arda",  "Güler",  "arda@student.be",  "Bachelor TI");
        savedScan(bedrijf2, "Lotte", "Mertens", "lotte@student.be", "Bachelor TI");

        // Enkel event1 opvragen: alleen Cegeka en zijn scan zichtbaar
        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Cegeka"))
                .andExpect(jsonPath("$[0].scanCount").value(1));
    }

    @Test
    void getBedrijvenWithScans_scannedAtIsPresent() throws Exception {
        HandshakeEvent event = savedEvent("Event");
        Bedrijf bedrijf = savedBedrijf("Cegeka", "IT", "cegeka@test.be", event);
        savedScan(bedrijf, "Arda", "Güler", "arda@student.be", "Bachelor TI");

        mockMvc.perform(get("/api/bedrijf/by-event/{eventId}/with-scans", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scans[0].scannedAt").isNotEmpty());
    }
}
