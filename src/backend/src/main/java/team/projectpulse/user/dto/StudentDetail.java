package team.projectpulse.user.dto;

import java.util.List;

public record StudentDetail(
        Long id,
        String firstName,
        String lastName,
        String email,
        List<StudentTeamInfo> teams,
        List<Object> peerEvaluations,
        List<Object> wars
) {}
