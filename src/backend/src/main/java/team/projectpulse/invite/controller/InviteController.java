package team.projectpulse.invite.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.invite.dto.InvitePreviewRequest;
import team.projectpulse.invite.dto.InviteSendRequest;
import team.projectpulse.invite.dto.InviteSendResult;
import team.projectpulse.invite.service.InviteService;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.User;

@RestController
@RequestMapping("${api.endpoint.base-url}/sections/{sectionId}/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/preview")
    public Result<InvitePreview> preview(@PathVariable Long sectionId,
                                         @Valid @RequestBody InvitePreviewRequest request,
                                         Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return Result.success(inviteService.preview(sectionId, request.emailsInput(), admin));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send")
    public Result<InviteSendResult> send(@PathVariable Long sectionId,
                                          @Valid @RequestBody InviteSendRequest request,
                                          Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return Result.success("Invitations sent successfully.",
                inviteService.send(sectionId, request.emails(), admin));
    }
}
