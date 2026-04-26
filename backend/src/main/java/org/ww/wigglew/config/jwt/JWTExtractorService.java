package org.ww.wigglew.config.jwt;


import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class JWTExtractorService  extends JWTService{
    /**
     * Extract user's phone number (i.e. unique identifier from the JWT Token)
     * @param jwtToken : The input token.
     * @return user's phone number encoded into the payload of the JWT Token.
     */
    public String extractUsername(String jwtToken){
        return extractSingleClaim(jwtToken, Claims::getSubject); //subject would be phone number of the user as we returned phone as username in UserEntity.UserDetails
    }
}
