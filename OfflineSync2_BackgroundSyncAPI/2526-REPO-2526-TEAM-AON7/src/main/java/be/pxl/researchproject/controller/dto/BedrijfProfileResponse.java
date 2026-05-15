package be.pxl.researchproject.controller.dto;

public record BedrijfProfileResponse(
        String firstname,
        String lastname,
        String email,
        String description,
        String sector,
        String website,
        String phoneNumber,
        String location,
        boolean hasJoinedEvent,
        Long joinedEventId
) {
}