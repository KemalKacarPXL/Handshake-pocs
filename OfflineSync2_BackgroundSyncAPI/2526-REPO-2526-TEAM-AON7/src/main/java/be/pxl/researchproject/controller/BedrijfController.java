package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.service.BedrijfService;

import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bedrijf")
public class BedrijfController {
    private final BedrijfService bedrijfService;

    @Autowired
    public BedrijfController(BedrijfService bedrijfService) {
        this.bedrijfService = bedrijfService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<BedrijfDTO>> getAllBedrijven() {
        return ResponseEntity.ok(bedrijfService.getAllBedrijven());
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<BedrijfDTO>> getBedrijvenByEvent(@org.springframework.web.bind.annotation.PathVariable Long eventId) {
        return ResponseEntity.ok(bedrijfService.getBedrijvenByEvent(eventId));
    }

    @GetMapping("/by-event/{eventId}/with-scans")
    public ResponseEntity<List<BedrijfWithScansDTO>> getBedrijvenWithScansByEvent(
            @org.springframework.web.bind.annotation.PathVariable Long eventId) {
        return ResponseEntity.ok(bedrijfService.getBedrijvenWithScansByEvent(eventId));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> createBedrijf(@Valid @RequestBody BedrijfRequest request) {
        bedrijfService.createBedrijf(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<BedrijfProfileResponse> getBedrijfProfile(@RequestParam String email) {
        BedrijfProfileResponse profile = bedrijfService.getBedrijfProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<BedrijfProfileResponse> updateBedrijfProfile(@RequestBody BedrijfProfileUpdateRequest request) {
        BedrijfProfileResponse updated = bedrijfService.updateBedrijfProfile(request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/join-event")
    public ResponseEntity<JoinEventResponse> joinEvent(@RequestHeader("bedrijf-email") String email,
            @RequestBody JoinEventRequest request) {
        JoinEventResponse response = bedrijfService.joinEvent(email, request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/scan")
    public ResponseEntity<Void> registerScan(
            @RequestParam String studentEmail,
            @RequestHeader("bedrijf-email") String bedrijfEmail) {
        boolean registered = bedrijfService.registerScan(studentEmail, bedrijfEmail);
        if (!registered) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scans")
    public ResponseEntity<List<BedrijfScanResponseDTO>> getScans(
            @RequestHeader("bedrijf-email") String bedrijfEmail) {
        return ResponseEntity.ok(bedrijfService.getScansForBedrijf(bedrijfEmail));
    }

    @PostMapping("/sync/scans")
    public ResponseEntity<SyncResponse> syncScans(
            @RequestBody List<OfflineScanDTO> offlineScans,
            @RequestHeader("bedrijf-email") String bedrijfEmail) {

        List<String> synced = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (OfflineScanDTO scan : offlineScans) {
            try {
                bedrijfService.registerScan(scan.studentEmail(), bedrijfEmail);
                synced.add(scan.localId());
            } catch (Exception e) {
                // Duplicate of andere fout — toch als synced markeren
                synced.add(scan.localId());
            }
        }

        return ResponseEntity.ok(new SyncResponse(synced, failed));
    }
}