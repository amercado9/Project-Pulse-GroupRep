package team.projectpulse.invite.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InviteSendRequest(@NotEmpty List<String> emails) {}
