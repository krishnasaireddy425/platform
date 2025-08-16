package com.company.platform.web.dto;

import java.util.UUID;

public record AcceptInviteRequest(UUID inviteId, String email, String tempPassword, String newPassword) {
}