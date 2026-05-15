package be.pxl.researchproject.controller;

import java.util.List;

public record SyncResponse(
        List<String> syncedIds,
        List<String> failedIds
) {}
