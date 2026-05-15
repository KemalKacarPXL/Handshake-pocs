package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.CreateHandshakeEventRequest;
import be.pxl.researchproject.controller.dto.HandshakeEventDTO;
import be.pxl.researchproject.service.HandshakeEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class HandshakeEventController {

    private final HandshakeEventService handshakeEventService;

    public HandshakeEventController(HandshakeEventService handshakeEventService) {
        this.handshakeEventService = handshakeEventService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody CreateHandshakeEventRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Titel is verplicht"));
        }
        if (request.date() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Datum is verplicht"));
        }
        if (request.startTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Starttijd is verplicht"));
        }
        if (request.endTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Eindtijd is verplicht"));
        }
        if (request.location() == null || request.location().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Locatie is verplicht"));
        }

        HandshakeEventDTO created = handshakeEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<HandshakeEventDTO>> getAllEvents() {
        return ResponseEntity.ok(handshakeEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        return handshakeEventService.getEventById(id)
                .map(event -> ResponseEntity.ok((Object) event))
                .orElse(ResponseEntity.notFound().build());
    }
}
