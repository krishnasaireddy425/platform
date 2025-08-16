package com.company.platform.web.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword) {
}