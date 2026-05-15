package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.CreateHandshakeEventRequest;
import be.pxl.researchproject.controller.dto.HandshakeEventDTO;
import be.pxl.researchproject.model.HandshakeEvent;

public class HandshakeEventMapper {

    private HandshakeEventMapper() {
    }

    public static HandshakeEvent toEntity(CreateHandshakeEventRequest request) {
        return new HandshakeEvent(
                request.title(),
                request.date(),
                request.startTime(),
                request.endTime(),
                request.location()
        );
    }

    public static HandshakeEventDTO toDTO(HandshakeEvent event) {
        return new HandshakeEventDTO(
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation()
        );
    }
}
