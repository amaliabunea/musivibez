package backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import backend.domain.User;

import java.time.Instant;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("iamawesome")
    private String secret;

    public UserInfo extractUser(final String token) {
        final Claims claims = extractAllClaims(token);
        User user = new User();
        user.setUsername(claims.getSubject());
        return new UserInfo(user);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String generateToken(UserInfo userInfo) {
        final Instant now = Instant.now();
        final Instant expiration = now.plusSeconds(10 * 60 * 60);
        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

}
