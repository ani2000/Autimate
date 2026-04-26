package org.ww.wigglew.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.function.Function;

abstract public class JWTService {
    @Value("${PRIVATE_KEY}")
    private String PRIVATE_KEY;

    protected Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(getSinginKey()).build().parseClaimsJws(token).getBody();
    }

    protected  <T> T extractSingleClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    protected Key getSinginKey(){
        byte[] keyBytes = Decoders.BASE64.decode(PRIVATE_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
