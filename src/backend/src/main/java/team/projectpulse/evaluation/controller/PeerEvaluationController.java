package team.projectpulse.evaluation.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.evaluation.dto.EvaluationWorkspace;
import team.projectpulse.evaluation.dto.PeerEvaluationSubmissionDetail;
import team.projectpulse.evaluation.dto.PeerEvaluationSubmissionRequest;
import team.projectpulse.evaluation.service.EvaluationService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/peer-evaluations")
public class PeerEvaluationController {

    private final EvaluationService evaluationService;

    public PeerEvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/workspace")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<EvaluationWorkspace> getWorkspace(Authentication authentication) {
        return Result.success(evaluationService.loadWorkspace(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PeerEvaluationSubmissionDetail> createSubmission(
        Authentication authentication,
        @RequestBody PeerEvaluationSubmissionRequest request
    ) {
        return Result.success(
            "Peer evaluation has been submitted.",
            evaluationService.createSubmission(authentication.getName(), request)
        );
    }

    @PutMapping("/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PeerEvaluationSubmissionDetail> updateSubmission(
        Authentication authentication,
        @PathVariable Long submissionId,
        @RequestBody PeerEvaluationSubmissionRequest request
    ) {
        return Result.success(
            "Peer evaluation has been updated.",
            evaluationService.updateSubmission(authentication.getName(), submissionId, request)
        );
    }
}
