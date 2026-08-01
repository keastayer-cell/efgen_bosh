package com.efgenbosh.backend.dto.directory;

import com.efgenbosh.backend.domain.Contractor;

public record ContractorResponse(Long id, String code, String shortName, String fullName, String phone, String signerName,
    String inn, String ogrnip, String address, String bankName, String bankInn, String bankKpp, String bik,
    String correspondentAccount, String settlementAccount) {
    public static ContractorResponse from(Contractor c) { return new ContractorResponse(c.getId(), c.getCode(), c.getShortName(), c.getFullName(), c.getPhone(), c.getSignerName(), c.getInn(), c.getOgrnip(), c.getAddress(), c.getBankName(), c.getBankInn(), c.getBankKpp(), c.getBik(), c.getCorrespondentAccount(), c.getSettlementAccount()); }
}
