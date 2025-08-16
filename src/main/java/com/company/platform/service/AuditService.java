package com.company.platform.service;

import com.company.platform.domain.AuditLog;
import com.company.platform.repo.AuditLogRepo;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepo repo;

    public AuditService(AuditLogRepo repo) {
        this.repo = repo;
    }

    public void log(UUID orgId, UUID actor, String action, String targetType, String targetId, String metaJson) {
        var log = new AuditLog();
        log.setOrgId(orgId);
        log.setActorUserId(actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setMeta(metaJson);
        repo.save(log);
    }
}