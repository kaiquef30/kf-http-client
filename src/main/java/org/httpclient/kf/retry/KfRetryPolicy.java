package org.httpclient.kf.retry;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class KfRetryPolicy {

    private final int maxAttempts;
    private final Duration baseDelay;
    private final Strategy strategy;
    private final Set<Integer> retryStatus;

    public enum Strategy {
        FIXED, LINEAR, EXPONENTIAL
    }

    private KfRetryPolicy(int maxAttempts, Duration baseDelay, Strategy strategy, Set<Integer> retryStatus) {
        this.maxAttempts = maxAttempts;
        this.baseDelay = baseDelay;
        this.strategy = strategy;
        this.retryStatus = retryStatus;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getBaseDelay() {
        return baseDelay;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public boolean shouldRetry(int statusCode) {
        return retryStatus.contains(statusCode);
    }

    public long getDelayForAttempt(int attempt) {
        switch (strategy) {
            case LINEAR:
                return baseDelay.toMillis() * attempt;
            case EXPONENTIAL:
                return baseDelay.toMillis() * (long) Math.pow(2, attempt - 1);
            case FIXED:
            default:
                return baseDelay.toMillis();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxAttempts = 3;
        private Duration baseDelay = Duration.ofMillis(500);
        private Strategy strategy = Strategy.FIXED;
        private final Set<Integer> retryStatus = new HashSet<>();

        public Builder maxAttempts(int attempts) {
            this.maxAttempts = attempts;
            return this;
        }

        public Builder baseDelay(Duration delay) {
            this.baseDelay = delay;
            return this;
        }

        public Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder retryOnStatus(Integer... codes) {
            for (int code : codes) {
                retryStatus.add(code);
            }
            return this;
        }

        public KfRetryPolicy build() {
            return new KfRetryPolicy(maxAttempts, baseDelay, strategy, retryStatus);
        }
    }
}
