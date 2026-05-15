package be.pxl.researchproject.integration;

import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.HandshakeEventRepository;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BedrijfEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BedrijfRepository bedrijfRepository;

    @Autowired
    private HandshakeEventRepository handshakeEventRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student student;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        bedrijfRepository.deleteAll();
        handshakeEventRepository.deleteAll();

        HandshakeEvent event1;
        HandshakeEvent event2;

        event1 = new HandshakeEvent("Event 1", LocalDate.now(), LocalTime.of(10,0), LocalTime.of(16,0), "Hasselt");
        event1 = handshakeEventRepository.save(event1);

        event2 = new HandshakeEvent("Event 2", LocalDate.now(), LocalTime.of(10,0), LocalTime.of(16,0), "Genk");
        event2 = handshakeEventRepository.save(event2);

        // student aanmaken die gekoppeld is aan eerste event
        student = new Student();
        student.setFirstname("Piet");
        student.setLastname("Peeters");
        student.setEmail("piet@student.be");
        student.setJoinedEvent(event1);
        studentRepository.save(student);

        // bedrijf die in hetzelfde evenement zit, en dus getoond moet worden
        Bedrijf bedrijf1 = new Bedrijf();
        bedrijf1.setFirstname("Jan");
        bedrijf1.setLastname("Janssens");
        bedrijf1.setEmail("jan@bedrijf.be");
        bedrijf1.setSector("IT");
        bedrijf1.setJoinedEvent(event1);
        bedrijfRepository.save(bedrijf1);

        // bedrijf dat in een ander evenement zit mag niet in de lijst staan
        Bedrijf bedrijf2 = new Bedrijf();
        bedrijf2.setFirstname("Els");
        bedrijf2.setLastname("Peeters");
        bedrijf2.setEmail("els@bedrijf.be");
        bedrijf2.setSector("HR");
        bedrijf2.setJoinedEvent(event2);
        bedrijfRepository.save(bedrijf2);
    }

    @Test
    void getBedrijvenByEvent_ReturnsOnlyBedrijvenInSameEventAsStudent() throws Exception {
        mockMvc.perform(get("/api/bedrijf/by-event/" + student.getJoinedEventId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("jan@bedrijf.be"))
                .andExpect(jsonPath("$[0].sector").value("IT"));
    }
}