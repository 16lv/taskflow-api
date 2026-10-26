package agency.shss7.taskflow.service;

import agency.shss7.taskflow.domain.Role;
import agency.shss7.taskflow.domain.UserAccount;
import agency.shss7.taskflow.dto.AuthDtos.*;
import agency.shss7.taskflow.repository.UserAccountRepository;
import agency.shss7.taskflow.security.JwtTokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
        UserAccountRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        Role assignedRole = request.role() != null ? request.role() : Role.MEMBER;
        UserAccount account = new UserAccount(
            request.email(),
            request.fullName(),
            passwordEncoder.encode(request.password()),
            assignedRole
        );
        UserAccount saved = userRepository.save(account);
        String token = jwtTokenService.issueToken(saved);
        return new AuthResponse(
            saved.getId(),
            saved.getEmail(),
            saved.getFullName(),
            saved.getRole(),
            token,
            jwtTokenService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount account = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtTokenService.issueToken(account);
        return new AuthResponse(
            account.getId(),
            account.getEmail(),
            account.getFullName(),
            account.getRole(),
            token,
            jwtTokenService.getExpirationSeconds()
        );
    }
}
