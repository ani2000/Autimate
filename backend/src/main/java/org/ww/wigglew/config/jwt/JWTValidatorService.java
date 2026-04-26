package org.ww.wigglew.config.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTValidatorService extends JWTService {

    @Autowired JWTExtractorService jwtExtractorService;

    public boolean isValidToken(String token, UserDetails userDetails){
        final String username = jwtExtractorService.extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractSingleClaim(token, Claims::getExpiration);
    }
}
