package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.CompanyInviteDTO;
import be.pxl.researchproject.controller.dto.CreateCompanyInviteRequest;
import be.pxl.researchproject.controller.dto.SendCompanyInviteEmailRequest;
import be.pxl.researchproject.model.CompanyInvite;

import be.pxl.researchproject.service.CompanyInviteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/company-invites")
public class CompanyInviteController {

    private final CompanyInviteService companyInviteService;

    public CompanyInviteController(CompanyInviteService companyInviteService) {
        this.companyInviteService = companyInviteService;
    }

    @PostMapping
    public ResponseEntity<?> createInvite(@RequestBody CreateCompanyInviteRequest request,
                                          HttpServletRequest httpRequest) {
        if (request.companyName() == null || request.companyName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bedrijfsnaam is verplicht"));
        }
        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mailadres is verplicht"));
        }
        if (!request.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ongeldig e-mailadres"));
        }

        String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName();
        int port = httpRequest.getServerPort();
        if (port != 80 && port != 443) {
            baseUrl += ":" + port;
        }

        CompanyInviteDTO created = companyInviteService.createInvite(request, baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendInviteEmail(@RequestBody SendCompanyInviteEmailRequest request) {
        try {
            companyInviteService.sendInviteEmail(request);
            return ResponseEntity.ok(Map.of("message", "E-mail verzonden"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "E-mail verzenden mislukt"));
        }
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        Optional<CompanyInvite> invite = companyInviteService.validateToken(token);
        if (invite.isEmpty()) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", "Deze uitnodigingslink is ongeldig of verlopen."));
        }
        CompanyInvite valid = invite.get();
        return ResponseEntity.ok(Map.of(
                "companyName", valid.getCompanyName(),
                "email", valid.getEmail()
        ));
    }
}
