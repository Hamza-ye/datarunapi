package org.nmcpye.datarun.analytics.etl.pivot;


import lombok.Data;

@Data
public class BuildResponse {
    private String status;
    private String message;
    private long durationSeconds;

    public static BuildResponse success(long secs) {
        BuildResponse r = new BuildResponse();
        r.setStatus("success");
        r.setDurationSeconds(secs);
        r.setMessage("build completed");
        return r;
    }

    public static BuildResponse failure(String msg) {
        BuildResponse r = new BuildResponse();
        r.setStatus("failed");
        r.setMessage(msg);
        return r;
    }

    public static BuildResponse failure(Throwable ex) {
        return failure(ex.getMessage());
    }

    public static BuildResponse failure(String msg, Object vr1, Object vr2) {
        BuildResponse r = new BuildResponse();
        r.setStatus("failed_validation");
        r.setMessage(msg + " | " + vr1 + " | " + vr2);
        return r;
    }
}
