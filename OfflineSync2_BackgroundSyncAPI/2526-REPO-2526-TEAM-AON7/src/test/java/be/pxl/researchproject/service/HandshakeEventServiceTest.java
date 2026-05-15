package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.CreateHandshakeEventRequest;
import be.pxl.researchproject.controller.dto.HandshakeEventDTO;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandshakeEventServiceTest {

    @Mock
    private HandshakeEventRepository handshakeEventRepository;

    @InjectMocks
    private HandshakeEventService handshakeEventService;

    private HandshakeEvent event;

    @BeforeEach
    void setUp() {
        event = new HandshakeEvent();
        event.setId(1L);
        event.setTitle("PXL IT Jobfair");
        event.setDate(LocalDate.of(2026, 3, 20));
        event.setStartTime(LocalTime.of(9, 0));
        event.setEndTime(LocalTime.of(16, 0));
        event.setLocation("Building D");
    }

    @Test
    void testCreateEvent() {
        CreateHandshakeEventRequest request = new CreateHandshakeEventRequest(
                "PXL IT Jobfair",
                LocalDate.of(2026, 3, 20),
                LocalTime.of(9, 0),
                LocalTime.of(16, 0),
                "Building D"
        );

        when(handshakeEventRepository.save(any(HandshakeEvent.class))).thenReturn(event);

        HandshakeEventDTO result = handshakeEventService.createEvent(request);

        assertThat(result.title()).isEqualTo("PXL IT Jobfair");
        assertThat(result.location()).isEqualTo("Building D");
        verify(handshakeEventRepository, times(1)).save(any(HandshakeEvent.class));
    }

    @Test
    void testGetAllEvents() {
        when(handshakeEventRepository.findAllByOrderByDateDescStartTimeDesc())
                .thenReturn(List.of(event));

        List<HandshakeEventDTO> result = handshakeEventService.getAllEvents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("PXL IT Jobfair");
        verify(handshakeEventRepository, times(1)).findAllByOrderByDateDescStartTimeDesc();
    }

    @Test
    void testGetEventById_Found() {
        when(handshakeEventRepository.findById(1L)).thenReturn(Optional.of(event));

        Optional<HandshakeEventDTO> result = handshakeEventService.getEventById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("PXL IT Jobfair");
        verify(handshakeEventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEventById_NotFound() {
        when(handshakeEventRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<HandshakeEventDTO> result = handshakeEventService.getEventById(1L);

        assertThat(result).isNotPresent();
        verify(handshakeEventRepository, times(1)).findById(1L);
    }
}
