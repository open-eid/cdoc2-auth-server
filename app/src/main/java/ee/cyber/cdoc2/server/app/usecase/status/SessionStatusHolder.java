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
    private final String cert;

    public SessionStatusHolder(GetSidSession.Response response) {
        this.state = response.state();
        this.endResult = response.endResult();
        this.sidSessionResponse = response;
        this.cert = getSigningCertificate(response).value();
    }

    public SessionStatusHolder(GetMidSession.Response response) {
        this.state = response.state();
        this.endResult = response.endResult();
        this.sidSessionResponse = null;
        this.cert = response.cert();
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

    private GetSidSession.Certificate getSigningCertificate(GetSidSession.Response response) {
        if (response.cert() == null) {
            throw new RuntimeException("Certificate missing in SID session response");
        }

        return response.cert();
    }
}
