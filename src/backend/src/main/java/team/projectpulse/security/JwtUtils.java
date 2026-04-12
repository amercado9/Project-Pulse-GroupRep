package team.projectpulse.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final byte[] secretKey;

    @Value("${security.jwt.token-expiry-in-hours:24}")
    private long expiryHours;

    public JwtUtils(@Value("${security.jwt.secret-key}") String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 requires at least 256 bits (32 bytes)
        this.secretKey = Arrays.copyOf(raw, Math.max(raw.length, 32));
    }

    public String generateToken(UserDetails user) {
        try {
            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .claim("roles", roles)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(expiryHours, ChronoUnit.HOURS)))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secretKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secretKey))) return false;
            Date expiry = jwt.getJWTClaimsSet().getExpirationTime();
            return expiry != null && expiry.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }
}
