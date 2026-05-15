package be.pxl.researchproject.controller.dto;

import java.util.List;

public record SyncResponse(
        List<String> syncedIds,
        List<String> failedIds
) {}
