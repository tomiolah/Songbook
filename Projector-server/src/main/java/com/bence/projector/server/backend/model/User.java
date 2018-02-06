package com.bence.projector.server.backend.model;

import java.util.Date;

public class User extends BaseEntity {

    private String password;
    private String email;
    private String phone;
    private Role role;
    private String token;
    private Date expiryDate;
    private String preferredLanguage;

    public User() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate == null ? null : (Date) expiryDate.clone();
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate == null ? null : (Date) expiryDate.clone();
    }

    @Override
    public String toString() {
        return "User{" + "password='" + password + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\''
                + ", role=" + role + '}';
    }

    public String getPreferredLanguage() {
        if (preferredLanguage == null) {
            preferredLanguage = "en";
        }
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
