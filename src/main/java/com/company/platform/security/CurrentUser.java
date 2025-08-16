package com.company.platform.security;

import java.util.UUID;

public record CurrentUser(UUID id, String email, String subType) {
    public boolean isPlatformOwner() {
        return "PLATFORM".equals(subType);
    }

    public boolean isUser() {
        return "USER".equals(subType);
    }
}