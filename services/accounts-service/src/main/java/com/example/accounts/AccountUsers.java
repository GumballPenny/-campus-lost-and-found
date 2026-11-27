package com.example.accounts;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class AccountUsers implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public AccountUsers(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override
    public UserDetails loadUserByUsername(String username) {
        return jdbc.query("select username,password_hash,role from users where username=?",
                (rs,n) -> User.withUsername(rs.getString("username"))
                        .password(rs.getString("password_hash")).roles(rs.getString("role")).build(),username)
                .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("bad username or password"));
    }
}
