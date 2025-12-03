package com.example.hr.service;

import com.example.hr.entity.ActivityLog;
import com.example.hr.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repo;

    public ActivityLogService(ActivityLogRepository repo) {
        this.repo = repo;
    }

    public void log(String action, String desc, String user) {
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setDescription(desc);
        log.setPerformedBy(user);
        repo.save(log);
    }
}
