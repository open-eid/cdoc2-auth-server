package ee.cyber.cdoc2.server.app.usecase.status.sid;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record SidSession(GetSidSession.Response response) {
    private static final String SESSION_COMPLETE = "COMPLETE";
    private static final String SESSION_RUNNING = "RUNNING";
    private static final String SESSION_END_RESULT_OK = "OK";

    public boolean isRunning() {
        return SESSION_RUNNING.equals(response.state());
    }

    public boolean isCompletedOk() {
        return SESSION_COMPLETE.equals(response.state())
            && SESSION_END_RESULT_OK.equals(response.endResult());
    }

    public boolean isCompletedNotOk() {
        return SESSION_COMPLETE.equals(response.state())
            && !SESSION_END_RESULT_OK.equals(response.endResult());
    }
}
