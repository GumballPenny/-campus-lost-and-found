package com.example.lostfound.security;
import com.example.lostfound.config.LostItemService;
import com.example.lostfound.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
@SpringJUnitConfig(MethodSecurityTest.Config.class)
class MethodSecurityTest {
    @Configuration @EnableMethodSecurity(proxyTargetClass = true) @Import(LostItemService.class) static class Config {}
    @Autowired LostItemService service;
    @MockitoBean LostItemRepository repository;
    @MockitoBean LostItemRule rules;
    @Test @WithMockUser(roles="FINDER")
    void juniorCannotBypassController() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.markFound(LostItemId.newId(),"finder"));
        verifyNoInteractions(repository,rules);
    }
}
