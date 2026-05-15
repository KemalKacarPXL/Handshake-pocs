package be.pxl.researchproject.controller.dto;

public record SendCompanyInviteEmailRequest(String coordinatorEmail, String token, String inviteLink) {
}
