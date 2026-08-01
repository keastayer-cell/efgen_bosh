package com.efgenbosh.backend.domain;

import java.util.Arrays;

public enum RepairCaseStatus {
    CREATED(1, "Создан"),
    WAITING_PARTS(2, "Ждём детали"),
    PARTS_RECEIVED(3, "Детали поступили"),
    SCHEDULED(4, "Запись на ремонт"),
    IN_REPAIR(5, "Ремонт"),
    READY(6, "Готов к выдаче"),
    DELIVERED(7, "Выдан"),
    CLOSED(8, "Закрыт");

    private final int id;
    private final String label;

    RepairCaseStatus(int id, String label) { this.id = id; this.label = label; }
    public int id() { return id; }
    public String code() { return name(); }
    public String label() { return label; }

    public static RepairCaseStatus parse(String value) {
        if (value == null || value.isBlank()) return null;
        try { return value.matches("\\d+") ? Arrays.stream(values()).filter(item -> item.id == Integer.parseInt(value)).findFirst().orElse(null) : valueOf(value.trim().toUpperCase()); }
        catch (IllegalArgumentException exception) { return null; }
    }
}
