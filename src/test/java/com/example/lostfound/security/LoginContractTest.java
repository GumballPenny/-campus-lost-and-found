package com.example.lostfound.security;
import com.example.lostfound.handler.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(AuthController.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes={AuthController.class,ApiExceptionHandler.class})
@Import({SecurityConfiguration.class,SecurityErrors.class,DevUsers.class,JwtConfiguration.class,TokenService.class})
class LoginContractTest {
    @Autowired MockMvc mvc;
    @Test void correctPasswordIssuesBearer() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"username\":\"finder\",\"password\":\"campus-dev\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900)).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
    @Test void wrongNameAndWrongPasswordShareMessage() throws Exception {
        for(String body:new String[]{
                "{\"username\":\"unknown\",\"password\":\"campus-dev\"}",
                "{\"username\":\"finder\",\"password\":\"wrong\"}"}) {
            mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(body))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("bad username or password"));
        }
    }
    @Test void blankLoginIs400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
