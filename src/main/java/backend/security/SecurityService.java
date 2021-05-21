package backend.security;

import lombok.AllArgsConstructor;
import lombok.var;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import backend.domain.User;
import backend.repository.UserRepository;

@Component
@AllArgsConstructor
public class SecurityService implements UserDetailsService {

    private UserRepository userRepository;
    private final ThreadLocal<UserInfo> actingUser = new ThreadLocal<>();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var optional = userRepository.findById(username);
        if (!optional.isPresent()) {
            throw new UsernameNotFoundException(username);
        }
        return new UserInfo(optional.get());
    }

    public void registerUser(String username, String password) {
        userRepository.save(new User(username, password));
    }

    public void setCurrentUser(UserInfo userInfo) {
        actingUser.set(userInfo);
    }

    public UserInfo getCurrentUser() {
        return actingUser.get();
    }
}