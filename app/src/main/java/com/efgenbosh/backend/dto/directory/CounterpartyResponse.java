package com.efgenbosh.backend.dto.directory;

import com.efgenbosh.backend.domain.Counterparty;

public record CounterpartyResponse(Long id, String name, String inn, String address, String phone, String note) {
    public static CounterpartyResponse from(Counterparty item) {
        return new CounterpartyResponse(item.getId(), item.getName(), item.getInn(), item.getAddress(), item.getPhone(), item.getNote());
    }
}
