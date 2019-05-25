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
    private Boolean activated;
    private String sureName;
    private String firstName;
    private String activationCode;
    private Boolean banned;

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

    public Boolean getActivated() {
        return activated;
    }

    public boolean isActivated() {
        return activated == null ? false : activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getSureName() {
        return sureName;
    }

    public void setSureName(String sureName) {
        this.sureName = sureName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public Boolean isBanned() {
        return banned != null && banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }
}
