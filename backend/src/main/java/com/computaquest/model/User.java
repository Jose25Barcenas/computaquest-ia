package com.computaquest.model;

import com.computaquest.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Builder.Default
    private String avatar = "avatar1";

    private String grade;

    @Builder.Default
    private Role role = Role.STUDENT;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Integer xp = 0;

    @Indexed
    @Builder.Default
    private Integer points = 0;

    @Builder.Default
    private Integer streak = 1;

    private Instant lastLoginDate;

    @Indexed
    private String resetToken;

    private Instant resetTokenExpiry;

    @Builder.Default
    private List<String> badges = List.of();

    @Builder.Default
    private boolean accountLocked = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @Transient
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
