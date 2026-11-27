package com.example.lostfound.handler;

import com.example.lostfound.config.LostItemUseCase;
import com.example.lostfound.domain.DuplicateLostItem;
import com.example.lostfound.domain.LostItem;
import com.example.lostfound.domain.LostItemId;
import com.example.lostfound.domain.LostItemKey;
import com.example.lostfound.domain.LostItemNotFound;
import com.example.lostfound.domain.LostItemStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.example.lostfound.security.SecurityConfiguration;
import com.example.lostfound.security.SecurityErrors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LostItemController.class)
@ContextConfiguration(classes = {LostItemController.class, ApiExceptionHandler.class})
@Import({SecurityConfiguration.class, SecurityErrors.class})
class LostItemControllerTest {
    @MockitoBean
    private JwtDecoder decoder;
    private static final UUID ID = UUID.fromString("d98956a8-3bce-4fa5-b50d-6c73820cfc38");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LostItemUseCase service;

    @Test
    void createReturns201LocationAndBody() throws Exception {
        when(service.register(any(), any(), any())).thenReturn(item());

        mvc.perform(post("/api/v1/lost-items")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"LF-WEB-1","title":"Black phone"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/" + ID)))
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.businessKey").value("LF-WEB-1"))
                .andExpect(jsonPath("$.status").value("REPORTED"));
    }

    @Test
    void blankTitleReturns400AndDoesNotCallService() throws Exception {
        mvc.perform(post("/api/v1/lost-items")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER")))
                        .header("X-Correlation-Id", "demo-correlation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"LF-WEB-1","title":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")))
                .andExpect(jsonPath("$.correlationId").value("demo-correlation"));

        verify(service, never()).register(any(), any(), any());
    }

    @Test
    void missingItemReturns404() throws Exception {
        when(service.find(any())).thenThrow(new LostItemNotFound(ID.toString()));

        mvc.perform(get("/api/v1/lost-items/{id}", ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedMissingItemReturns404() throws Exception {
        when(service.find(any())).thenThrow(new LostItemNotFound(ID.toString()));
        mvc.perform(get("/api/v1/lost-items/{id}", ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.register(any(), any(), any())).thenThrow(new DuplicateLostItem("LF-WEB-1"));

        mvc.perform(post("/api/v1/lost-items")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"LF-WEB-1","title":"Black phone"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void nonUuidPathReturns400() throws Exception {
        mvc.perform(get("/api/v1/lost-items/not-a-uuid")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("id must be a UUID"));
    }

    @Test
    void getReturns200AndItem() throws Exception {
        when(service.find(any())).thenReturn(item());

        mvc.perform(get("/api/v1/lost-items/{id}", ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FINDER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.title").value("Black phone"));
    }

    private LostItem item() {
        return new LostItem(new LostItemId(ID), new LostItemKey("LF-WEB-1"), LostItemStatus.REPORTED, "Black phone");
    }
}
