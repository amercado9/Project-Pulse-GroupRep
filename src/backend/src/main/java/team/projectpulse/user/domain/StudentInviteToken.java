package team.projectpulse.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_invite_tokens")
public class StudentInviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getToken()                   { return token; }
    public void setToken(String token)         { this.token = token; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public Long getSectionId()                 { return sectionId; }
    public void setSectionId(Long sectionId)   { this.sectionId = sectionId; }

    public LocalDateTime getExpiresAt()        { return expiresAt; }
    public void setExpiresAt(LocalDateTime v)  { this.expiresAt = v; }

    public boolean isUsed()                    { return used; }
    public void setUsed(boolean used)          { this.used = used; }
}
