package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.BedrijfScanResponseDTO;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import be.pxl.researchproject.service.BedrijfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final ScanRepository scanRepository;
    private final BedrijfRepository bedrijfRepository;
    private final StudentRepository studentRepository;
    private final BedrijfService bedrijfService;

    @Autowired
    public SyncController(ScanRepository scanRepository, BedrijfRepository bedrijfRepository, StudentRepository studentRepository,  BedrijfService bedrijfService) {
        this.scanRepository = scanRepository;
        this.bedrijfRepository = bedrijfRepository;
        this.studentRepository = studentRepository;
        this.bedrijfService = bedrijfService;
    }

    @PostMapping("/scans")
    public ResponseEntity<SyncResponse> syncScans(
            @RequestBody List<OfflineScanDTO> offlineScans,
            @RequestHeader("Authorization") String token) {

        List<String> synced = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (OfflineScanDTO offlineScan : offlineScans) {
            try {
                boolean registered = bedrijfService.registerScan(
                        offlineScan.studentEmail(),
                        offlineScan.bedrijfEmail()
                );
                if (registered || offlineScan.alreadySynced()) {
                    synced.add(offlineScan.localId());
                } else {
                    synced.add(offlineScan.localId()); // duplicate is ook ok
                }
            } catch (Exception e) {
                failed.add(offlineScan.localId());
            }
        }

        return ResponseEntity.ok(new SyncResponse(synced, failed));
    }

    // Geeft alle data terug die de bedrijf nodig heeft offline
    @GetMapping("/initial-data")
    public ResponseEntity<InitialDataResponse> getInitialData(
            @RequestParam String bedrijfEmail) {

        List<BedrijfScanResponseDTO> scans =
                bedrijfService.getScansForBedrijf(bedrijfEmail);

        return ResponseEntity.ok(new InitialDataResponse(scans));
    }
}

