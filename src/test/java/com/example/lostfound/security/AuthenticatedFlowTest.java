package com.example.lostfound.security;
import com.example.lostfound.config.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(classes=Application.class)
@AutoConfigureMockMvc
class AuthenticatedFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    String login(String name) throws Exception {
        String body=mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                .content("{\"username\":\""+name+"\",\"password\":\"campus-dev\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }
    @Test void realTokensProtectPersistentStatusAndActor() throws Exception {
        String junior=login("finder"), senior=login("custodian");
        String key="FLOW-"+UUID.randomUUID();
        String body="{\"businessKey\":\""+key+"\",\"title\":\"Flow phone\"}";
        String response=mvc.perform(post("/api/v1/lost-items").contentType("application/json")
                        .header("Authorization","Bearer "+junior).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id=json.readTree(response).get("id").asText();
        mvc.perform(post("/api/v1/lost-items").contentType("application/json")
                        .header("Authorization","Bearer "+junior).content(body)).andExpect(status().isConflict());
        assertEquals(1,jdbc.queryForObject("select count(*) from lost_item where business_key=?",Integer.class,key));
        mvc.perform(post("/api/v1/lost-items/"+id+"/found").header("Authorization","Bearer "+junior))
                .andExpect(status().isForbidden());
        assertEquals("REPORTED",jdbc.queryForObject("select status from lost_item where id=?::uuid",String.class,id));
        mvc.perform(post("/api/v1/lost-items/"+id+"/found").header("Authorization","Bearer "+senior))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FOUND"));
        assertEquals("custodian",jdbc.queryForObject("select actor from lost_item_event where item_id=?::uuid",String.class,id));
        mvc.perform(post("/api/v1/lost-items/"+id+"/found").header("Authorization","Bearer "+senior))
                .andExpect(status().isConflict());
        assertEquals(1,jdbc.queryForObject("select count(*) from lost_item_event where item_id=?::uuid",Integer.class,id));
    }
}
