package com.efgenbosh.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.efgenbosh.backend.service.ApiAccessRuleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiAccessRuleFilterTest {

    private final ApiAccessRuleService accessRuleService =
        mock(ApiAccessRuleService.class);
    private final ApiAccessRuleFilter filter = new ApiAccessRuleFilter(
        accessRuleService,
        new ObjectMapper().findAndRegisterModules()
    );

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicHealthDoesNotRequireDynamicRule() throws Exception {
        MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        verify(accessRuleService, never()).isAllowed(
            List.of(), "GET", "/api/health"
        );
    }

    @Test
    void deniedRequestUsesStandardErrorEnvelope() throws Exception {
        authenticate("ROLE_VIEWER");
        when(
            accessRuleService.isAllowed(
                List.of("VIEWER"),
                "DELETE",
                "/api/orders/1"
            )
        ).thenReturn(false);
        MockHttpServletRequest request =
            new MockHttpServletRequest("DELETE", "/api/orders/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains(
            "\"status\":403",
            "\"error\":\"Доступ к API запрещен для вашей роли.\"",
            "\"path\":\"/api/orders/1\""
        );
    }

    private void authenticate(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "user",
                "n/a",
                List.of(new SimpleGrantedAuthority(authority))
            )
        );
    }
}
