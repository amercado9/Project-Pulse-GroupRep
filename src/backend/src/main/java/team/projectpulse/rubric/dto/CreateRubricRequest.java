package team.projectpulse.rubric.dto;

import java.util.List;

public record CreateRubricRequest(String rubricName, List<CriterionRequest> criteria) {}
