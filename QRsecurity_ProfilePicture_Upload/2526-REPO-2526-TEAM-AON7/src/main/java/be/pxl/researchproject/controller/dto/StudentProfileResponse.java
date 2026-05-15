package be.pxl.researchproject.controller.dto;

public record StudentProfileResponse(
        String firstname,
        String lastname,
        String email,
        String education,
        boolean hasCv,
        String cvFileName,
        Long cvFileSize,
        boolean hasJoinedEvent,
        Long joinedEventId,
        boolean hasProfilePicture
) {
}