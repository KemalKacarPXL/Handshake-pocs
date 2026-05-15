package be.pxl.researchproject.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateHandshakeEventRequest(String title, LocalDate date, LocalTime startTime, LocalTime endTime, String location) {
}
