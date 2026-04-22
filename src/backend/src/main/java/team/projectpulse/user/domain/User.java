package team.projectpulse.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.projectpulse.section.domain.Section;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /** Space-separated roles, e.g. "admin instructor" or "student" */
    @Column(nullable = false)
    private String roles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    private boolean enabled;

    // ── UserDetails ──────────────────────────────────────────────────────────

    @Override
    @JsonIgnore
    public String getUsername() { return email; }

    @Override
    @JsonIgnore
    public String getPassword() { return password; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() { return true; }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split("\\s+"))
                .filter(r -> !r.isBlank())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toList());
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getFirstName()             { return firstName; }
    public void setFirstName(String v)       { this.firstName = v; }

    public String getLastName()              { return lastName; }
    public void setLastName(String v)        { this.lastName = v; }

    public String getEmail()                 { return email; }
    public void setEmail(String v)           { this.email = v; }

    public void setPassword(String v)        { this.password = v; }

    public String getRoles()                 { return roles; }
    public void setRoles(String v)           { this.roles = v; }

    public Section getSection()              { return section; }
    public void setSection(Section v)        { this.section = v; }

    public void setEnabled(boolean v)        { this.enabled = v; }
}
