   package be.pxl.researchproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import be.pxl.researchproject.model.Bedrijf;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface BedrijfRepository extends JpaRepository<Bedrijf, Long> {
    Optional<Bedrijf> findByEmail(String email);

    List<Bedrijf> findByJoinedEvent_Id(Long eventId);

    boolean existsByEmail(String email);

    @Query("""
            select b.firstname as firstname,
                   b.lastname as lastname,
                   b.email as email,
                   b.description as description,
                   b.sector as sector,
                   b.website as website,
                   b.phoneNumber as phoneNumber,
                   b.location as location
            from Bedrijf b
            where b.email = :email
            """)
    Optional<BedrijfProfileProjection> findProfileByEmail(@Param("email") String email);

    interface BedrijfProfileProjection{
        String getFirstname();
        String getLastname();
        String getEmail();
        String getDescription();
        String getSector();
        String getWebsite();
        String getPhoneNumber();
        String getLocation();
    }
}