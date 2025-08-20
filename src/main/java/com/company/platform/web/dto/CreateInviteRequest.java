package com.company.platform.web.dto;

public record CreateInviteRequest(
        String email,
        String roleName) {
}