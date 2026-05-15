package be.pxl.researchproject.repository;

import be.pxl.researchproject.model.Scan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanRepository extends JpaRepository<Scan, Long> {
    List<Scan> findByBedrijfEmail(String email);
    List<Scan> findByStudentEmail(String email);
    List<Scan> findByBedrijf_JoinedEvent_Id(Long eventId);
}