package com.efgenbosh.backend.dto.car;

import java.time.LocalDate;
import java.time.LocalTime;

public record RepairCaseActionRequest(Long contractorId, LocalDate appointmentDate, LocalTime appointmentTime, String receivedBy, String comment) {}
