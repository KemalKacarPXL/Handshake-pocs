package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.CreateHandshakeEventRequest;
import be.pxl.researchproject.controller.dto.HandshakeEventDTO;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HandshakeEventService {

    private final HandshakeEventRepository handshakeEventRepository;

    public HandshakeEventService(HandshakeEventRepository handshakeEventRepository) {
        this.handshakeEventRepository = handshakeEventRepository;
    }

    public HandshakeEventDTO createEvent(CreateHandshakeEventRequest request) {
        HandshakeEvent event = HandshakeEventMapper.toEntity(request);
        HandshakeEvent savedEvent = handshakeEventRepository.save(event);
        return HandshakeEventMapper.toDTO(savedEvent);
    }

    public List<HandshakeEventDTO> getAllEvents() {
        return handshakeEventRepository.findAllByOrderByDateDescStartTimeDesc()
                .stream()
                .map(HandshakeEventMapper::toDTO)
                .toList();
    }

    public Optional<HandshakeEventDTO> getEventById(Long id) {
        return handshakeEventRepository.findById(id)
                .map(HandshakeEventMapper::toDTO);
    }
}
