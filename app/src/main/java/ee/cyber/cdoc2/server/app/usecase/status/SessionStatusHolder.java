package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.Getter;

import org.jspecify.annotations.Nullable;

import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

@Getter
public class SessionStatusHolder {
    private static final String SESSION_COMPLETE = "COMPLETE";
    private static final String SESSION_RUNNING = "RUNNING";
    private static final String SESSION_END_RESULT_OK = "OK";

    private final String state;
    @Nullable
    private final String endResult;
    private final GetSidSession.@Nullable Response sidSessionResponse;
    private final GetMidSession.@Nullable Response midSessionResponse;

    public SessionStatusHolder(GetSidSession.Response response) {
        this.state = response.state();
        this.endResult = response.endResult();
        this.sidSessionResponse = response;
        this.midSessionResponse = null;
    }

    public SessionStatusHolder(GetMidSession.Response response) {
        this.state = response.state();
        this.endResult = response.endResult();
        this.sidSessionResponse = null;
        this.midSessionResponse = response;
    }

    public boolean isRunning() {
        return SESSION_RUNNING.equals(state);
    }

    public boolean isCompletedOk() {
        return SESSION_COMPLETE.equals(state)
            && SESSION_END_RESULT_OK.equals(endResult);
    }

    public boolean isCompletedNotOk() {
        return SESSION_COMPLETE.equals(state)
            && !SESSION_END_RESULT_OK.equals(endResult);
    }

    public String getCert() {
        if (sidSessionResponse != null && midSessionResponse != null) {
            throw new IllegalStateException("SessionSatusHolder in illegal state");
        }

        String certValue = null;

        if (sidSessionResponse != null) {
            certValue = getSidSigningCertificate(sidSessionResponse);
        }

        if (midSessionResponse != null) {
            certValue = midSessionResponse.cert();
        }

        if (certValue == null) {
            throw new IllegalStateException("Certificate value is null");
        }

        return certValue;
    }

    private String getSidSigningCertificate(GetSidSession.Response response) {
        if (response.cert() == null) {
            throw new IllegalStateException("Certificate missing in SID session response");
        }

        return response.cert().value();
    }
}
