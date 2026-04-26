package team.projectpulse.invite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InstructorInviteSendRequest(
        @NotEmpty List<String> emails,
        @NotBlank String subject,
        @NotBlank String body
) {}
