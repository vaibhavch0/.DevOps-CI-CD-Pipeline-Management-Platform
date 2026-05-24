package com.autorabit.pipeline.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Application user with role-based access control.
 * Roles: ADMIN (full access), DEVELOPER (trigger + view), VIEWER (read-only)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ---- Constructors ----

    public User() {}

    private User(Builder b) {
        this.username  = b.username;
        this.email     = b.email;
        this.password  = b.password;
        this.fullName  = b.fullName;
        this.avatarUrl = b.avatarUrl;
        this.roles     = b.roles != null ? b.roles : new HashSet<>();
        this.active    = b.active;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String avatarUrl;
        private Set<String> roles = new HashSet<>();
        private boolean active = true;

        public Builder username(String u)       { this.username = u; return this; }
        public Builder email(String e)          { this.email = e; return this; }
        public Builder password(String p)       { this.password = p; return this; }
        public Builder fullName(String n)       { this.fullName = n; return this; }
        public Builder avatarUrl(String url)    { this.avatarUrl = url; return this; }
        public Builder roles(Set<String> r)     { this.roles = r; return this; }
        public Builder active(boolean a)        { this.active = a; return this; }
        public User build()                     { return new User(this); }
    }

    // ---- Getters & Setters ----

    public Long getId()                          { return id; }
    public String getUsername()                  { return username; }
    public void setUsername(String u)            { this.username = u; }
    public String getEmail()                     { return email; }
    public void setEmail(String e)               { this.email = e; }
    public String getPassword()                  { return password; }
    public void setPassword(String p)            { this.password = p; }
    public String getFullName()                  { return fullName; }
    public void setFullName(String n)            { this.fullName = n; }
    public String getAvatarUrl()                 { return avatarUrl; }
    public void setAvatarUrl(String url)         { this.avatarUrl = url; }
    public Set<String> getRoles()                { return roles; }
    public void setRoles(Set<String> roles)      { this.roles = roles; }
    public boolean isActive()                    { return active; }
    public void setActive(boolean active)        { this.active = active; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getLastLoginAt()        { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime t)  { this.lastLoginAt = t; }
}
