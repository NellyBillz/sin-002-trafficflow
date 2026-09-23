package co.wethinkcode.trafficflow;

import java.time.Duration;
import java.time.Instant;

public final class WatchdogState {

    private final Duration timeout;
    private volatile Instant lastHeartbeat;

    public WatchdogState(Duration timeout) {
        this.timeout = timeout;
    }

    public void recordHeartbeat(Instant receivedAt) {
        lastHeartbeat = receivedAt;
    }

    public boolean isAlerting() {
        Instant heartbeat = lastHeartbeat;
        return heartbeat == null
                || Duration.between(heartbeat, Instant.now()).compareTo(timeout) > 0;
    }

    public String lastHeartbeat() {
        return lastHeartbeat == null ? null : lastHeartbeat.toString();
    }
}
