package be.pxl.researchproject.repository;

import be.pxl.researchproject.model.CompanyInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyInviteRepository extends JpaRepository<CompanyInvite, Long> {

    Optional<CompanyInvite> findByToken(String token);

    boolean existsByEmailIgnoreCaseAndInvitationEmailSentTrueAndIdNot(String email, Long id);

    Optional<CompanyInvite> findFirstByEmailIgnoreCaseOrderByIdDesc(String email);
}
