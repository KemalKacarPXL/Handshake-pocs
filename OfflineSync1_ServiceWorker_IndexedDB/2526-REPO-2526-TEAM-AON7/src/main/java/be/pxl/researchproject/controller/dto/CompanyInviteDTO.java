package be.pxl.researchproject.controller.dto;

import java.time.LocalDateTime;

public record CompanyInviteDTO(String inviteLink, String token, LocalDateTime expiresAt, boolean used) {
}
