package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.HandshakeEventRepository;

import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.CompanyInviteRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class BedrijfService {

    private final HandshakeEventRepository handshakeEventRepository;
    private final BedrijfRepository bedrijfRepository;
    private final ScanRepository scanRepository;
    private final StudentRepository studentRepository;
    private final CompanyInviteRepository companyInviteRepository;

    @Autowired
    public BedrijfService(BedrijfRepository bedrijfRepository,
                          HandshakeEventRepository handshakeEventRepository,
                          ScanRepository scanRepository,
                          StudentRepository studentRepository,
                          CompanyInviteRepository companyInviteRepository) {
        this.bedrijfRepository = bedrijfRepository;
        this.handshakeEventRepository = handshakeEventRepository;
        this.scanRepository = scanRepository;
        this.studentRepository = studentRepository;
        this.companyInviteRepository = companyInviteRepository;
    }

    @Transactional
    public JoinEventResponse joinEvent(String email, JoinEventRequest request) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        Bedrijf bedrijf = bedrijfRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Bedrijf niet gevonden"));
        HandshakeEvent event = handshakeEventRepository.findById(request.id())
                .orElseThrow(() -> new BadRequestException("Event niet gevonden"));
        if (bedrijf.hasJoinedEvent()) {
            return new JoinEventResponse(false, "Je neemt al deel aan een evenement.");
        }
        bedrijf.setJoinedEvent(event);
        bedrijfRepository.save(bedrijf);
        return new JoinEventResponse(true, "Je bent succesvol gejoined!");
    }

    @Transactional
    public BedrijfProfileResponse updateBedrijfProfile(be.pxl.researchproject.controller.dto.BedrijfProfileUpdateRequest request) {
        String normalizedOldEmail = request.getOldEmail().toLowerCase(Locale.ROOT).trim();
        Bedrijf bedrijf = bedrijfRepository.findByEmail(normalizedOldEmail)
            .orElseThrow(() -> new BadRequestException("Bedrijf niet gevonden"));

        bedrijf.setFirstname(request.getFirstname());
        bedrijf.setLastname(request.getLastname());
        bedrijf.setDescription(request.getDescription());
        bedrijf.setSector(request.getSector());
        bedrijf.setEmail(request.getEmail().toLowerCase(Locale.ROOT).trim());
        bedrijf.setWebsite(request.getWebsite());
        bedrijf.setPhoneNumber(request.getPhoneNumber());
        bedrijf.setLocation(request.getLocation());

        bedrijfRepository.save(bedrijf);

        // Return the updated profile
        boolean hasJoinedEvent = bedrijf.hasJoinedEvent();
        Long joinedEventId = bedrijf.getJoinedEventId();
        return new BedrijfProfileResponse(
                bedrijf.getFirstname(),
                bedrijf.getLastname(),
                bedrijf.getEmail(),
                bedrijf.getDescription(),
                bedrijf.getSector(),
                bedrijf.getWebsite(),
                bedrijf.getPhoneNumber(),
                bedrijf.getLocation(),
                hasJoinedEvent,
                joinedEventId
        );
    }

    public List<BedrijfDTO> getBedrijvenByEvent(Long eventId) {
        return bedrijfRepository.findByJoinedEvent_Id(eventId).stream()
                .map(BedrijfDTO::new)
                .toList();
    }

    public void createBedrijf(BedrijfRequest bedrijfRequest) {
        String normalEmail = bedrijfRequest.getEmail().toLowerCase().trim();

        if (bedrijfRepository.existsByEmail(normalEmail)) {
            throw new BadRequestException("Email is al in gebruik");
        }
        Bedrijf bedrijf = new Bedrijf();
        bedrijf.setFirstname(bedrijfRequest.getFirstname());
        bedrijf.setLastname(bedrijfRequest.getLastname());
        bedrijf.setEmail(normalEmail);
        bedrijf.setPassword(bedrijfRequest.getPassword());
        bedrijf.setCompanyName(resolveCompanyName(bedrijfRequest.getCompanyName(), normalEmail));
        bedrijfRepository.save(bedrijf);
    }

    private String resolveCompanyName(String requestedCompanyName, String email) {
        if (requestedCompanyName != null && !requestedCompanyName.isBlank()) {
            return requestedCompanyName.trim();
        }
        return companyInviteRepository.findFirstByEmailIgnoreCaseOrderByIdDesc(email)
                .map(invite -> invite.getCompanyName())
                .orElse(null);
    }

    public List<BedrijfDTO> getAllBedrijven() {
        return bedrijfRepository.findAll().stream()
                .map(BedrijfDTO::new)
                .toList();
    }

    public BedrijfProfileResponse getBedrijfProfileByEmail(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        Bedrijf bedrijf = bedrijfRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadRequestException("Bedrijf niet gevonden"));

        boolean hasJoinedEvent = bedrijf.hasJoinedEvent();
        Long joinedEventId = bedrijf.getJoinedEventId();

        return new BedrijfProfileResponse(
                bedrijf.getFirstname(),
                bedrijf.getLastname(),
                bedrijf.getEmail(),
                bedrijf.getDescription(),
                bedrijf.getSector(),
                bedrijf.getWebsite(),
                bedrijf.getPhoneNumber(),
                bedrijf.getLocation(),
                hasJoinedEvent,
                joinedEventId
        );
    }

    @Transactional
    public boolean registerScan(String studentEmail, String bedrijfEmail) {
        String normalizedStudentEmail = studentEmail.toLowerCase(Locale.ROOT).trim();
        String normalizedBedrijfEmail = bedrijfEmail.toLowerCase(Locale.ROOT).trim();

        Student student = studentRepository.findByEmail(normalizedStudentEmail)
                .orElseThrow(() -> new BadRequestException("Student niet gevonden"));

        Bedrijf bedrijf = bedrijfRepository.findByEmail(normalizedBedrijfEmail)
                .orElseThrow(() -> new BadRequestException("Bedrijf niet gevonden"));

        // Avoid duplicate scans
        List<Scan> existing = scanRepository.findByBedrijfEmail(normalizedBedrijfEmail);
        boolean alreadyScanned = existing.stream()
                .anyMatch(s -> s.getStudentEmail().equals(normalizedStudentEmail));
        if (alreadyScanned) {
            return false;
        }

        Scan scan = new Scan();
        scan.setStudentFirstname(student.getFirstname());
        scan.setStudentLastname(student.getLastname());
        scan.setStudentEmail(student.getEmail());
        scan.setStudentEducation(student.getEducation());
        scan.setScannedAt(LocalDateTime.now());
        scan.setBedrijf(bedrijf);
        scanRepository.save(scan);
        return true;
    }

    public List<BedrijfScanResponseDTO> getScansForBedrijf(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        return scanRepository.findByBedrijfEmail(normalizedEmail).stream()
                .map(s -> new BedrijfScanResponseDTO(
                        s.getStudentFirstname(),
                        s.getStudentLastname(),
                        s.getStudentEmail(),
                        s.getStudentEducation(),
                        s.getScannedAt()
                ))
                .toList();
    }

    public List<BedrijfWithScansDTO> getBedrijvenWithScansByEvent(Long eventId) {
        return bedrijfRepository.findByJoinedEvent_Id(eventId).stream()
                .map(bedrijf -> {
                    List<BedrijfScanResponseDTO> scans = scanRepository
                            .findByBedrijfEmail(bedrijf.getEmail()).stream()
                            .map(s -> new BedrijfScanResponseDTO(
                                    s.getStudentFirstname(),
                                    s.getStudentLastname(),
                                    s.getStudentEmail(),
                                    s.getStudentEducation(),
                                    s.getScannedAt()
                            ))
                            .toList();
                    return new BedrijfWithScansDTO(
                            bedrijf.getId(),
                            bedrijf.getCompanyName(),
                            bedrijf.getSector(),
                            bedrijf.getEmail(),
                            scans.size(),
                            scans
                    );
                })
                .toList();
    }
}
