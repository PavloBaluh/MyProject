package jarvizz.project.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jarvizz.project.models.AccountCredentials;
import jarvizz.project.models.Roles;
import jarvizz.project.models.SuperUser;
import jarvizz.project.models.User;
import jarvizz.project.sevices.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class AdminLoginFilter extends AbstractAuthenticationProcessingFilter {
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    public AdminLoginFilter(String url, AuthenticationManager authManager, UserService userService, PasswordEncoder passwordEncoder) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    private AccountCredentials creds;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        creds = new ObjectMapper().readValue(httpServletRequest.getInputStream(), AccountCredentials.class);
        if (creds.getUsername().equals(SuperUser.getInstance().getUsername()) && creds.getPassword().equals(SuperUser.getInstance().getPassword())) {
            User byName = userService.findByName(creds.getUsername());
            User user = new User(creds.getUsername(), passwordEncoder.encode(creds.getPassword()), Roles.ROLE_ADMIN, true);
            if (byName == null) {
                userService.save(user);
            }
            return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(),user.getAuthorities()));
        }
        else return null;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) {
        String jwtoken = Jwts.builder()
                .setSubject(auth.getName())
                .signWith(SignatureAlgorithm.HS512, "yes".getBytes())
                .compact();
        res.addHeader("Authorization", "Bearer " + jwtoken);

    }
}
