package ee.cyber.cdoc2.server.app.usecase.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.app.exception.InputValidationException;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessStatus;
import ee.cyber.cdoc2.server.app.usecase.common.AuthProcessType;
import ee.cyber.cdoc2.server.app.usecase.status.mid.CreateMidSessionToken;
import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.CreateSidSessionToken;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStatusImplTest {

    private static final String VALID_UUID_STR = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID VALID_UUID = UUID.fromString(VALID_UUID_STR);
    private static final String SESSION_UUID_STR = "987fcdeb-a654-0987-fedc-ba9876543210";

    @Mock private GetAuthProcess getAuthProcess;
    @Mock private FailAuthProcess failAuthProcess;
    @Mock private CompleteAuthProcess completeAuthProcess;
    @Mock private GetSidSession getSidSession;
    @Mock private GetMidSession getMidSession;
    @Mock private CreateMidSessionToken createMidSessionToken;
    @Mock private CreateSidSessionToken createSidSessionToken;

    @InjectMocks private GetStatusImpl getStatusImpl;

    @Test
    void invalidUuidThrowsInputValidationException() {
        assertThrows(InputValidationException.class, () -> getStatusImpl.execute("not-a-uuid"));
    }

    @Test
    void failedAuthProcessReturnsFailedResponse() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.FAILED, AuthProcessType.SID,
                "USER_CANCELLED", null, null, null)
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.FAILED.name(), response.status());
        assertEquals("USER_CANCELLED", response.endResult());
        assertNull(response.sessionToken());
    }

    @Test
    void completeAuthProcessReturnsCompleteResponseWithToken() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.COMPLETE, AuthProcessType.SID,
                "OK", null, "existing-token", "signing-cert")
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.COMPLETE.name(), response.status());
        assertEquals("OK", response.endResult());
        assertEquals("existing-token", response.sessionToken());
        assertEquals("signing-cert", response.signingCertificate());
    }

    @Test
    void startedProcessWithNullSessionUuidThrowsIllegalState() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.SID,
                null, null, null, null)
        );

        assertThrows(IllegalStateException.class, () -> getStatusImpl.execute(VALID_UUID_STR));
    }

    @Test
    void startedSidSessionRunningReturnsStartedResponse() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.SID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getSidSession.execute(any())).thenReturn(
            new GetSidSession.Response("RUNNING", null, null, null, null)
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.STARTED.name(), response.status());
        assertNull(response.sessionToken());
    }

    @Test
    void startedSidSessionCompletedNotOkFailsAndReturnsFailed() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.SID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getSidSession.execute(any())).thenReturn(
            new GetSidSession.Response("COMPLETE", "USER_REFUSED", null, null, null)
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.FAILED.name(), response.status());
        assertEquals("USER_REFUSED", response.endResult());
        verify(failAuthProcess).execute(any(FailAuthProcess.Request.class));
    }

    @Test
    void startedSidSessionCompletedOkCreatesTokenAndReturnsComplete() {
        var cert = new GetSidSession.Certificate("sid-cert-pem", "QUALIFIED");
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.SID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getSidSession.execute(any())).thenReturn(
            new GetSidSession.Response("COMPLETE", "OK", null, cert, null)
        );
        when(createSidSessionToken.execute(any(), any())).thenReturn("signed-sd-jwt");

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.COMPLETE.name(), response.status());
        assertEquals("OK", response.endResult());
        assertEquals("signed-sd-jwt", response.sessionToken());
        assertEquals("sid-cert-pem", response.signingCertificate());
        verify(completeAuthProcess).execute(any(CompleteAuthProcess.Request.class));
    }

    @Test
    void startedMidSessionRunningReturnsStartedResponse() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.MID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getMidSession.execute(any())).thenReturn(
            new GetMidSession.Response("RUNNING", null, null, null)
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.STARTED.name(), response.status());
        assertNull(response.sessionToken());
    }

    @Test
    void startedMidSessionCompletedNotOkFailsAndReturnsFailed() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.MID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getMidSession.execute(any())).thenReturn(
            new GetMidSession.Response("COMPLETE", "TIMEOUT", null, null)
        );

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.FAILED.name(), response.status());
        assertEquals("TIMEOUT", response.endResult());
        verify(failAuthProcess).execute(any(FailAuthProcess.Request.class));
    }

    @Test
    void startedMidSessionCompletedOkCreatesTokenAndReturnsComplete() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.MID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getMidSession.execute(any())).thenReturn(
            new GetMidSession.Response("COMPLETE", "OK", "mid-cert-pem", null)
        );
        when(createMidSessionToken.execute(any(), any())).thenReturn("signed-sd-jwt");

        GetStatus.Response response = getStatusImpl.execute(VALID_UUID_STR);

        assertEquals(AuthProcessStatus.COMPLETE.name(), response.status());
        assertEquals("OK", response.endResult());
        assertEquals("signed-sd-jwt", response.sessionToken());
        assertEquals("mid-cert-pem", response.signingCertificate());
        verify(completeAuthProcess).execute(any(CompleteAuthProcess.Request.class));
    }

    @Test
    void startedSessionWithUnknownSessionStateThrowsIllegalState() {
        when(getAuthProcess.execute(VALID_UUID)).thenReturn(
            new GetAuthProcess.Response(AuthProcessStatus.STARTED, AuthProcessType.SID,
                null, SESSION_UUID_STR, null, null)
        );
        when(getSidSession.execute(any())).thenReturn(
            new GetSidSession.Response("EXPIRED", null, null, null, null)
        );

        assertThrows(IllegalStateException.class, () -> getStatusImpl.execute(VALID_UUID_STR));
    }
}
