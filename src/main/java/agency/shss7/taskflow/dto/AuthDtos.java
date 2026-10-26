package agency.shss7.taskflow.dto;

import agency.shss7.taskflow.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 90, message = "Full name must be between 2 and 90 characters")
        String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be at least 8 characters")
        String password,

        Role role
    ) {}

    public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
    ) {}

    public record AuthResponse(
        Long userId,
        String email,
        String fullName,
        Role role,
        String accessToken,
        long expiresInSeconds
    ) {}
}
