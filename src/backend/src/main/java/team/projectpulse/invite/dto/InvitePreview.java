package team.projectpulse.invite.dto;

import java.util.List;

public record InvitePreview(List<String> emails, int emailCount, String subject, String body) {}
