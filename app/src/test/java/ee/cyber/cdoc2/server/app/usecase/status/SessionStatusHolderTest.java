package ee.cyber.cdoc2.server.app.usecase.status;

import org.junit.jupiter.api.Test;

import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStatusHolderTest {

    @Test
    void isRunningWhenSidSessionStateIsRunning() {
        var response = new GetSidSession.Response("RUNNING", null, null, null, null);
        var holder = new SessionStatusHolder(response);

        assertTrue(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
    }

    @Test
    void isRunningWhenMidSessionStateIsRunning() {
        var response = new GetMidSession.Response("RUNNING", null, null, null);
        var holder = new SessionStatusHolder(response);

        assertTrue(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
    }

    @Test
    void isCompletedOkWhenSidStateIsCompleteAndEndResultIsOk() {
        var response = new GetSidSession.Response("COMPLETE", "OK", null, null, null);
        var holder = new SessionStatusHolder(response);

        assertFalse(holder.isRunning());
        assertTrue(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
    }

    @Test
    void isCompletedNotOkWhenSidStateIsCompleteAndEndResultIsNotOk() {
        var response = new GetSidSession.Response("COMPLETE", "USER_REFUSED", null, null, null);
        var holder = new SessionStatusHolder(response);

        assertFalse(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertTrue(holder.isCompletedNotOk());
    }

    @Test
    void isCompletedNotOkWhenMidStateIsCompleteAndEndResultIsNotOk() {
        var response = new GetMidSession.Response("COMPLETE", "TIMEOUT", null, null);
        var holder = new SessionStatusHolder(response);

        assertFalse(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertTrue(holder.isCompletedNotOk());
    }

    @Test
    void getCertReturnsSidCertificateValue() {
        var cert = new GetSidSession.Certificate("sid-cert-pem", "QUALIFIED");
        var response = new GetSidSession.Response("COMPLETE", "OK", null, cert, null);
        var holder = new SessionStatusHolder(response);

        assertEquals("sid-cert-pem", holder.getCert());
    }

    @Test
    void getCertReturnsMidCertificateValue() {
        var response = new GetMidSession.Response("COMPLETE", "OK", "mid-cert-pem", null);
        var holder = new SessionStatusHolder(response);

        assertEquals("mid-cert-pem", holder.getCert());
    }

    @Test
    void getCertThrowsWhenSidCertIsNull() {
        var response = new GetSidSession.Response("COMPLETE", "OK", null, null, null);
        var holder = new SessionStatusHolder(response);

        assertThrows(IllegalStateException.class, holder::getCert);
    }

    @Test
    void getCertThrowsWhenMidCertIsNull() {
        var response = new GetMidSession.Response("COMPLETE", "OK", null, null);
        var holder = new SessionStatusHolder(response);

        assertThrows(IllegalStateException.class, holder::getCert);
    }

    @Test
    void sidHolderHasNullMidSessionResponse() {
        var response = new GetSidSession.Response("RUNNING", null, null, null, null);
        var holder = new SessionStatusHolder(response);

        assertNotNull(holder.getSidSessionResponse());
        assertNull(holder.getMidSessionResponse());
    }

    @Test
    void midHolderHasNullSidSessionResponse() {
        var response = new GetMidSession.Response("RUNNING", null, null, null);
        var holder = new SessionStatusHolder(response);

        assertNull(holder.getSidSessionResponse());
        assertNotNull(holder.getMidSessionResponse());
    }
}
