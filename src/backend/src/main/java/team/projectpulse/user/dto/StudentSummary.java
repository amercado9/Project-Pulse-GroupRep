package team.projectpulse.user.dto;

import java.util.List;

public record StudentSummary(Long id, String firstName, String lastName, List<String> teamNames) {}
