package team.projectpulse.auth.dto;

public record StudentRegisterRequest(
        String token,
        String firstName,
        String lastName,
        String password
) {}
