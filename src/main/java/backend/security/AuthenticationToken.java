package backend.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@AllArgsConstructor
public class AuthenticationToken implements Authentication {

    private UserDetails userInfo;
    private AuthenticationDetails details;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userInfo.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return userInfo.getPassword();
    }

    @Override
    @Deprecated
    public Object getDetails() {
        return details;
    }

    public AuthenticationDetails getAuthenticationDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return userInfo.getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        System.out.println("setAuthenticated(" + isAuthenticated + ")");
    }

    @Override
    public String getName() {
        return userInfo.getUsername();
    }

    public void setDetails(AuthenticationDetails details) {
        this.details = details;
    }

    public static class AuthenticationDetails {

        public AuthenticationDetails(HttpServletRequest request) {

        }
    }
}
