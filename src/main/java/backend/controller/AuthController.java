package backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.requests.AuthenticationRequest;
import backend.requests.AuthenticationResponse;
import backend.requests.RegistrationRequest;
import backend.security.JWTUtil;
import backend.security.SecurityService;
import backend.security.UserInfo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private SecurityService securityService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest)
            throws Exception {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new Exception("Incorrect credentials", ex);
        }

        final UserInfo userInfo = (UserInfo) securityService.loadUserByUsername(authenticationRequest.getUsername());
        return ResponseEntity.ok(new AuthenticationResponse(jwtUtil.generateToken(userInfo)));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegistrationRequest registrationRequest)
            throws Exception {
        securityService.registerUser(registrationRequest.getUsername(), registrationRequest.getPassword());
        return authenticate(new AuthenticationRequest(
                registrationRequest.getUsername(), registrationRequest.getPassword()));
    }
}

