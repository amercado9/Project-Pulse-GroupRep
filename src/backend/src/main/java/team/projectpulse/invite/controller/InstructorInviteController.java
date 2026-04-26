package team.projectpulse.invite.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.invite.dto.InstructorInviteSendRequest;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.invite.dto.InvitePreviewRequest;
import team.projectpulse.invite.dto.InviteSendResult;
import team.projectpulse.invite.service.InstructorInviteService;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.User;

@RestController
@RequestMapping("${api.endpoint.base-url}/instructors/invites")
public class InstructorInviteController {

    private final InstructorInviteService instructorInviteService;

    public InstructorInviteController(InstructorInviteService instructorInviteService) {
        this.instructorInviteService = instructorInviteService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/preview")
    public Result<InvitePreview> preview(@Valid @RequestBody InvitePreviewRequest request,
                                          Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return Result.success(instructorInviteService.preview(request.emailsInput(), admin));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send")
    public Result<InviteSendResult> send(@Valid @RequestBody InstructorInviteSendRequest request,
                                          Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return Result.success("Invitations sent successfully.",
                instructorInviteService.send(request.emails(), request.subject(), request.body(), admin));
    }
}
