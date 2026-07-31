package com.efgenbosh.backend.service;

import com.efgenbosh.backend.domain.ApiAccessRule;
import com.efgenbosh.backend.domain.Role;
import com.efgenbosh.backend.domain.RoleCode;
import com.efgenbosh.backend.repository.ApiAccessRuleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiAccessRuleServiceTest {

    @Test
    void appliesRoleMatrixAndAlwaysAllowsAdmin() {
        ApiAccessRuleRepository repository = mock(ApiAccessRuleRepository.class);
        when(repository.findAllActiveWithRole()).thenReturn(List.of(
            rule(RoleCode.OPERATOR, "PUT", "/api/orders/**"),
            rule(RoleCode.VIEWER, "GET", "/api/orders/**")
        ));
        ApiAccessRuleService service = new ApiAccessRuleService(repository);

        assertThat(service.isAllowed(List.of("ADMIN"), "DELETE", "/api/admin/users/1")).isTrue();
        assertThat(service.isAllowed(List.of("OPERATOR"), "PUT", "/api/orders/1")).isTrue();
        assertThat(service.isAllowed(List.of("OPERATOR"), "GET", "/api/orders/1")).isFalse();
        assertThat(service.isAllowed(List.of("VIEWER"), "GET", "/api/orders/1")).isTrue();
        assertThat(service.isAllowed(List.of("VIEWER"), "POST", "/api/orders")).isFalse();
        assertThat(service.isAllowed(List.of(), "GET", "/api/orders")).isFalse();
    }

    private ApiAccessRule rule(RoleCode roleCode, String method, String pattern) {
        Role role = new Role();
        role.setCode(roleCode);
        ApiAccessRule rule = new ApiAccessRule();
        rule.setRole(role);
        rule.setHttpMethod(method);
        rule.setUrlPattern(pattern);
        return rule;
    }
}
