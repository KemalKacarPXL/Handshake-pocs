package be.pxl.researchproject.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HandshakeEventDTO(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime, String location) {
}
