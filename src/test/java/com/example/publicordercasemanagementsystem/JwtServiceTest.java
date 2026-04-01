package com.example.publicordercasemanagementsystem;

import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.util.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JwtServiceTest {

    @Test
    void shouldCreateAndParseToken() {
        JwtService jwtService = new JwtService("test-secret-key-32-chars-minimum!!", 60, 120);
        User user = new User();
        user.setId(1001L);
        user.setRole("police_officer");
        user.setRoleName("办案民警");
        user.setDepartment("XX派出所");
        user.setDepartmentId(501L);
        user.setName("张明");

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.parseClaims(token);

        Assertions.assertEquals("1001", claims.getSubject());
        Assertions.assertEquals("张明", claims.get("name", String.class));
        Assertions.assertEquals("police_officer", claims.get("role", String.class));
    }
}
