package com.company.platform.web.dto;

public record TokenResponse(String accessToken, boolean mustChangePassword) {
}