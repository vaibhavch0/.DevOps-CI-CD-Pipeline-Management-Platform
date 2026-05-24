package com.autorabit.pipeline.dto;

import java.util.Set;

/**
 * Login response with JWT token and user details.
 */
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Set<String> roles;
    private long expiresIn;

    public LoginResponse() {}

    private LoginResponse(Builder b) {
        this.token     = b.token;
        this.tokenType = b.tokenType;
        this.userId    = b.userId;
        this.username  = b.username;
        this.email     = b.email;
        this.fullName  = b.fullName;
        this.avatarUrl = b.avatarUrl;
        this.roles     = b.roles;
        this.expiresIn = b.expiresIn;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String tokenType;
        private Long userId;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private Set<String> roles;
        private long expiresIn;

        public Builder token(String t)         { this.token = t; return this; }
        public Builder tokenType(String t)     { this.tokenType = t; return this; }
        public Builder userId(Long id)         { this.userId = id; return this; }
        public Builder username(String u)      { this.username = u; return this; }
        public Builder email(String e)         { this.email = e; return this; }
        public Builder fullName(String n)      { this.fullName = n; return this; }
        public Builder avatarUrl(String url)   { this.avatarUrl = url; return this; }
        public Builder roles(Set<String> r)    { this.roles = r; return this; }
        public Builder expiresIn(long ms)      { this.expiresIn = ms; return this; }
        public LoginResponse build()           { return new LoginResponse(this); }
    }

    public String getToken()       { return token; }
    public String getTokenType()   { return tokenType; }
    public Long getUserId()        { return userId; }
    public String getUsername()    { return username; }
    public String getEmail()       { return email; }
    public String getFullName()    { return fullName; }
    public String getAvatarUrl()   { return avatarUrl; }
    public Set<String> getRoles()  { return roles; }
    public long getExpiresIn()     { return expiresIn; }
}
