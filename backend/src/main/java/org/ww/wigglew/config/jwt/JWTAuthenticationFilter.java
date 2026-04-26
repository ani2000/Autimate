package org.ww.wigglew.config.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The filter is like a middleware
 * and needed to be called on every request made on the server.
 * To make such thing, OncePerRequestFilter needed to be inherited.

 * Explaining what the model does:
 *  1. Check if JWT token available or not. If not, simply pass to the next filter to either reject or accept the request.
 *  2. If contain JWT token, extract token.
 *      Then extract userPhone number from JWT Token.
 *  3. Check user already authenticated or not. If not, fetch user info from DB.
 *  4. Fetch user from DB.
 *  5. Build Authentication token (not jwt token) and set some extra details based on request.
 *  6. Authenticate using SecurityContextHolder
 */

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTExtractorService jwtExtractorService;
    private final JWTValidatorService jwtValidatorService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt, userPhoneNumber;
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //extract JWT token.
        jwt = authHeader.substring(7);
        userPhoneNumber = jwtExtractorService.extractUsername(jwt);

        //check user is not authenticated already. Then pick user details from db.
        if(userPhoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userPhoneNumber);
            if(jwtValidatorService.isValidToken(jwt, userDetails)){

                //Generate auth token -> This is not JWT Token.
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //Authenticate
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
