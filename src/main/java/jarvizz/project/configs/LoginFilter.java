package jarvizz.project.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jarvizz.project.models.AccountCredentials;
import jarvizz.project.models.User;
import jarvizz.project.sevices.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;


public class LoginFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;

    public LoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }
    private AccountCredentials creds;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        creds = new ObjectMapper().readValue(httpServletRequest.getInputStream(), AccountCredentials.class);
        System.out.println(creds.getUsername());
        String name = creds.getUsername();
        System.out.println(name);
        User byName = userService.findByName(name);
        if (byName != null){
        if (passwordEncoder.encode(creds.getPassword()).equals(byName.getPassword())){
           return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), Collections.emptyList()));
        }
        else return null;
        }
        else return null;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {
        String jwtoken = Jwts.builder()
                .setSubject(auth.getName())
                .signWith(SignatureAlgorithm.HS512, "yes".getBytes())
//                .setExpiration(new Date(System.currentTimeMillis() + 200000))
                .compact();
        res.addHeader("Authorization", "Bearer " + jwtoken);

    }
}

