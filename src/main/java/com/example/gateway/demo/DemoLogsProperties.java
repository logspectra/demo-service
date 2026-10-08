package com.example.gateway.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.demo-logs")
public class DemoLogsProperties {

    private boolean enabled = true;
    private long fastIntervalMs = 400;
    private long mediumIntervalMs = 2000;
    private long slowIntervalMs = 7000;
    private long burstIntervalMs = 12000;
    /** How often to emit a batch of ERROR logs (ms). */
    private long errorIntervalMs = 1000;
    /** ERROR lines emitted each error-interval tick. */
    private int errorsPerTick = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getFastIntervalMs() {
        return fastIntervalMs;
    }

    public void setFastIntervalMs(long fastIntervalMs) {
        this.fastIntervalMs = fastIntervalMs;
    }

    public long getMediumIntervalMs() {
        return mediumIntervalMs;
    }

    public void setMediumIntervalMs(long mediumIntervalMs) {
        this.mediumIntervalMs = mediumIntervalMs;
    }

    public long getSlowIntervalMs() {
        return slowIntervalMs;
    }

    public void setSlowIntervalMs(long slowIntervalMs) {
        this.slowIntervalMs = slowIntervalMs;
    }

    public long getBurstIntervalMs() {
        return burstIntervalMs;
    }

    public void setBurstIntervalMs(long burstIntervalMs) {
        this.burstIntervalMs = burstIntervalMs;
    }

    public long getErrorIntervalMs() {
        return errorIntervalMs;
    }

    public void setErrorIntervalMs(long errorIntervalMs) {
        this.errorIntervalMs = errorIntervalMs;
    }

    public int getErrorsPerTick() {
        return errorsPerTick;
    }

    public void setErrorsPerTick(int errorsPerTick) {
        this.errorsPerTick = Math.max(1, Math.min(errorsPerTick, 20));
    }
}
