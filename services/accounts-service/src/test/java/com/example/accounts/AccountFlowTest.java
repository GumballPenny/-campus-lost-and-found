package com.example.accounts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountFlowTest {
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Value("${app.custodian-invite}") String invitation;
    @Test void registrationPersistsHashedUserAndLoginSurvivesNewRequest() {
        String name="user-"+UUID.randomUUID().toString().substring(0,12);
        var request=Map.of("username",name,"password","test-password-2026","displayName","Test finder");
        var created=http.postForEntity("/internal/auth/register",request,Map.class);
        assertEquals(201,created.getStatusCode().value());
        assertEquals("Bearer",created.getBody().get("tokenType"));
        String hash=jdbc.queryForObject("select password_hash from users where username=?",String.class,name);
        assertTrue(hash.startsWith("{bcrypt}"));
        assertFalse(hash.contains("test-password"));
        assertEquals("FINDER",jdbc.queryForObject("select role from users where username=?",String.class,name));
        var logged=http.postForEntity("/internal/auth/login",Map.of("username",name,"password","test-password-2026"),Map.class);
        assertEquals(200,logged.getStatusCode().value());
        assertNotNull(logged.getBody().get("accessToken"));
        assertEquals(409,http.postForEntity("/internal/auth/register",request,Map.class).getStatusCode().value());
        var bad=http.postForEntity("/internal/auth/login",Map.of("username",name,"password","wrong"),Map.class);
        var unknown=http.postForEntity("/internal/auth/login",Map.of("username","unknown-"+name,"password","wrong"),Map.class);
        assertEquals(401,bad.getStatusCode().value());
        assertEquals(bad.getBody().get("message"),unknown.getBody().get("message"));
        assertEquals(2,jdbc.queryForObject("select count(*) from auth_event a join users u on a.user_id=u.id where u.username=?",Integer.class,name));
    }
    @Test void seniorRequiresValidInvitation() {
        String name="lead-"+UUID.randomUUID().toString().substring(0,12);
        assertEquals(400,http.postForEntity("/internal/auth/register",
                Map.of("username",name,"password","test-password-2026","displayName","Lead","invitation","wrong"),Map.class).getStatusCode().value());
        assertEquals(201,http.postForEntity("/internal/auth/register",
                Map.of("username",name,"password","test-password-2026","displayName","Lead","invitation",invitation),Map.class).getStatusCode().value());
        assertEquals("CUSTODIAN",jdbc.queryForObject("select role from users where username=?",String.class,name));
    }
}
