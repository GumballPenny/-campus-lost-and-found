package com.example.lostfound.security;

import com.example.lostfound.handler.*;
import com.example.lostfound.config.LostItemUseCase;
import com.example.lostfound.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({LostItemController.class, MeController.class})
@ContextConfiguration(classes={LostItemController.class,MeController.class,ApiExceptionHandler.class})
@Import({SecurityConfiguration.class,SecurityErrors.class})
class SecurityContractTest {
    @Autowired MockMvc mvc;
    @MockitoBean LostItemUseCase service;
    @MockitoBean JwtDecoder decoder;
    private final UUID id=UUID.randomUUID();

    @Test void anonymousCannotRead() throws Exception {
        mvc.perform(get("/api/v1/lost-items/"+id).header("X-Correlation-Id","anonymous-check"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("valid token required"))
                .andExpect(jsonPath("$.correlationId").value("anonymous-check"));
        verifyNoInteractions(service);
    }
    @Test void juniorNeverCallsLockedService() throws Exception {
        mvc.perform(post("/api/v1/lost-items/"+id+"/found")
                        .header("X-Correlation-Id","junior-check")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("not allowed for your role"))
                .andExpect(jsonPath("$.correlationId").value("junior-check"));
        verifyNoInteractions(service);
    }
    @Test void seniorUsesTokenSubjectNotJsonActor() throws Exception {
        var item=new LostItem(new LostItemId(id),new LostItemKey("LF-SEC"),LostItemStatus.FOUND,"Phone");
        when(service.markFound(any(),eq("custodian"))).thenReturn(item);
        mvc.perform(post("/api/v1/lost-items/"+id+"/found")
                        .contentType("application/json").content("{\"actor\":\"attacker\",\"role\":\"CUSTODIAN\"}")
                        .with(jwt().jwt(j -> j.subject("custodian"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTODIAN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FOUND"));
        verify(service).markFound(new LostItemId(id),"custodian");
    }
    @Test void meUsesToken() throws Exception {
        mvc.perform(get("/api/me").with(jwt().jwt(j->j.subject("finder"))
                        .authorities(new SimpleGrantedAuthority("ROLE_FINDER"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("finder"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_FINDER"));
    }
    @Test void malformedBearerIs401() throws Exception {
        when(decoder.decode("broken")).thenThrow(new org.springframework.security.oauth2.jwt.BadJwtException("bad"));
        mvc.perform(get("/api/me").header("Authorization","Bearer broken"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
