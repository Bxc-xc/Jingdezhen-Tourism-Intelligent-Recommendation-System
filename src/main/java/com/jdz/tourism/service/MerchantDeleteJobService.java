package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantDeleteJobService {
    private static final Duration COMPLETED_JOB_TTL = Duration.ofMinutes(10);
    private static final int MAX_RETRY_ON_LOCK = 2;

    public enum JobStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED
    }

    public static class MerchantDeleteJob {
        private String jobId;
        private Long merchantId;
        private JobStatus status;
        private String errorMessage;
        private String phase;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public Long getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(Long merchantId) {
            this.merchantId = merchantId;
        }

        public JobStatus getStatus() {
            return status;
        }

        public void setStatus(JobStatus status) {
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }
    }

    private final Map<String, MerchantDeleteJob> jobs = new ConcurrentHashMap<>();
    private final Set<Long> merchantsInDeleting = ConcurrentHashMap.newKeySet();
    private final ExecutorService deleteExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
        private int idx = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "merchant-delete-worker-" + idx++);
            t.setDaemon(true);
            return t;
        }
    });

    @Autowired
    private MerchantService merchantService;

    public MerchantDeleteJob createDeleteJob(Long merchantId) {
        cleanupExpiredJobs();
        Optional<Merchant> merchantOpt = merchantService.getMerchantById(merchantId);
        if (merchantOpt.isEmpty()) {
            throw new RuntimeException("商家不存在");
        }
        if (!merchantsInDeleting.add(merchantId)) {
            throw new RuntimeException("该商家已有删除任务在执行，请稍后查看状态");
        }

        MerchantDeleteJob job = new MerchantDeleteJob();
        job.setJobId(UUID.randomUUID().toString().replace("-", ""));
        job.setMerchantId(merchantId);
        job.setStatus(JobStatus.PENDING);
        job.setPhase("queued");
        job.setCreatedAt(LocalDateTime.now());
        jobs.put(job.getJobId(), job);

        deleteExecutor.submit(() -> runDeleteJob(job.getJobId(), merchantId));
        return job;
    }

    public Optional<MerchantDeleteJob> getJob(String jobId) {
        cleanupExpiredJobs();
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void runDeleteJob(String jobId, Long merchantId) {
        MerchantDeleteJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }

        job.setStatus(JobStatus.RUNNING);
        job.setPhase("cleanup_children");
        job.setStartedAt(LocalDateTime.now());
        try {
            Exception lastError = null;
            for (int attempt = 1; attempt <= MAX_RETRY_ON_LOCK; attempt++) {
                try {
                    merchantService.deleteMerchant(merchantId);
                    job.setStatus(JobStatus.SUCCESS);
                    job.setPhase("done");
                    lastError = null;
                    break;
                } catch (Exception e) {
                    lastError = e;
                    if (!isLockRelatedError(e) || attempt == MAX_RETRY_ON_LOCK) {
                        break;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(300L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        lastError = ie;
                        break;
                    }
                }
            }

            if (job.getStatus() != JobStatus.SUCCESS) {
                job.setStatus(JobStatus.FAILED);
                job.setPhase("failed");
                String message = lastError != null ? lastError.getMessage() : null;
                job.setErrorMessage(message != null ? message : "删除失败");
            }
        } finally {
            merchantsInDeleting.remove(merchantId);
            job.setCompletedAt(LocalDateTime.now());
        }
    }

    private boolean isLockRelatedError(Throwable error) {
        if (error == null) {
            return false;
        }
        String name = error.getClass().getName();
        if (name.contains("CannotAcquireLockException")
                || name.contains("PessimisticLockingFailureException")
                || name.contains("LockTimeoutException")
                || name.contains("QueryTimeoutException")) {
            return true;
        }
        String message = error.getMessage();
        if (message != null) {
            String m = message.toLowerCase();
            if (m.contains("lock wait timeout")
                    || m.contains("deadlock")
                    || m.contains("could not obtain lock")
                    || m.contains("timeout")) {
                return true;
            }
        }
        return isLockRelatedError(error.getCause());
    }

    private void cleanupExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();
        jobs.entrySet().removeIf(entry -> {
            MerchantDeleteJob job = entry.getValue();
            if (job == null || job.getCompletedAt() == null) {
                return false;
            }
            JobStatus status = job.getStatus();
            if (status != JobStatus.SUCCESS && status != JobStatus.FAILED) {
                return false;
            }
            Duration age = Duration.between(job.getCompletedAt(), now);
            return age.compareTo(COMPLETED_JOB_TTL) > 0;
        });
    }

    @PreDestroy
    public void shutdownExecutor() {
        deleteExecutor.shutdown();
    }
}
