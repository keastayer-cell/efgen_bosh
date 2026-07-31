package com.efgenbosh.backend.repository;

import com.efgenbosh.backend.domain.ApiAccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApiAccessRuleRepository extends JpaRepository<ApiAccessRule, Long> {

    @Query("""
        select rule
        from ApiAccessRule rule
        join fetch rule.role role
        where rule.active = true
    """)
    List<ApiAccessRule> findAllActiveWithRole();
}
