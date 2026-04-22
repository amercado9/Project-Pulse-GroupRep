package team.projectpulse.invite.dto;

import jakarta.validation.constraints.NotBlank;

public record InvitePreviewRequest(@NotBlank String emailsInput) {}
