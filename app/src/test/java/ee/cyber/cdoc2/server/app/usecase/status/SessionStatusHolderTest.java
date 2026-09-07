package ee.cyber.cdoc2.server.app.usecase.status;

import org.junit.jupiter.api.Test;

import ee.cyber.cdoc2.server.app.usecase.status.mid.GetMidSession;
import ee.cyber.cdoc2.server.app.usecase.status.sid.GetSidSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStatusHolderTest {

    private static final String SID_CERT_VALUE = "MIID-sample-sid-cert";
    private static final String MID_CERT_VALUE = "MIID-sample-mid-cert";

    @Test
    void sidRunningIsRunning() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("RUNNING", null, SID_CERT_VALUE)
        );

        assertTrue(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
        assertEquals("RUNNING", holder.getState());
        assertNull(holder.getEndResult());
    }

    @Test
    void sidCompleteOkIsCompletedOk() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("COMPLETE", "OK", SID_CERT_VALUE)
        );

        assertFalse(holder.isRunning());
        assertTrue(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
        assertEquals("OK", holder.getEndResult());
    }

    @Test
    void sidCompleteUserRefusedIsNotOk() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("COMPLETE", "USER_REFUSED", SID_CERT_VALUE)
        );

        assertFalse(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertTrue(holder.isCompletedNotOk());
        assertEquals("USER_REFUSED", holder.getEndResult());
    }

    @Test
    void sidCompleteNullEndResultIsNotOk() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("COMPLETE", null, SID_CERT_VALUE)
        );

        assertFalse(holder.isRunning());
        assertFalse(holder.isCompletedOk());
        assertTrue(holder.isCompletedNotOk(),
            "COMPLETE with null endResult must be treated as not-OK to avoid the success path");
    }

    @Test
    void sidHolderStoresSidResponse() {
        GetSidSession.Response sidResp = sidResponse("COMPLETE", "OK", SID_CERT_VALUE);

        SessionStatusHolder holder = new SessionStatusHolder(sidResp);

        assertSame(sidResp, holder.getSidSessionResponse());
        assertNull(holder.getMidSessionResponse());
    }

    @Test
    void sidGetCertReturnsValue() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("COMPLETE", "OK", SID_CERT_VALUE)
        );

        assertEquals(SID_CERT_VALUE, holder.getCert());
    }

    @Test
    void sidGetCertThrowsWhenMissing() {
        SessionStatusHolder holder = new SessionStatusHolder(
            sidResponse("COMPLETE", "OK", null)
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, holder::getCert);
        assertEquals("Certificate missing in SID session response", ex.getMessage());
    }

    @Test
    void midCompleteOkIsCompletedOk() {
        SessionStatusHolder holder = new SessionStatusHolder(
            midResponse("COMPLETE", "OK", MID_CERT_VALUE)
        );

        assertFalse(holder.isRunning());
        assertTrue(holder.isCompletedOk());
        assertFalse(holder.isCompletedNotOk());
        assertEquals("COMPLETE", holder.getState());
        assertEquals("OK", holder.getEndResult());
    }

    @Test
    void midCompleteNullEndResultIsNotOk() {
        SessionStatusHolder holder = new SessionStatusHolder(
            midResponse("COMPLETE", null, null)
        );

        assertTrue(holder.isCompletedNotOk(),
            "COMPLETE with null endResult must be treated as not-OK in the MID branch too");
        assertFalse(holder.isCompletedOk());
    }

    @Test
    void midHolderStoresMidResponse() {
        GetMidSession.Response midResp = midResponse("COMPLETE", "OK", MID_CERT_VALUE);

        SessionStatusHolder holder = new SessionStatusHolder(midResp);

        assertSame(midResp, holder.getMidSessionResponse());
        assertNull(holder.getSidSessionResponse());
    }

    @Test
    void midGetCertReturnsValue() {
        SessionStatusHolder holder = new SessionStatusHolder(
            midResponse("COMPLETE", "OK", MID_CERT_VALUE)
        );

        assertEquals(MID_CERT_VALUE, holder.getCert());
    }

    @Test
    void midGetCertThrowsWhenNull() {
        SessionStatusHolder holder = new SessionStatusHolder(
            midResponse("COMPLETE", "OK", null)
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, holder::getCert);
        assertEquals("Certificate value is null", ex.getMessage());
    }

    private static GetSidSession.Response sidResponse(
        String state,
        String endResult,
        String certValue
    ) {
        GetSidSession.Certificate cert = certValue == null
            ? null
            : new GetSidSession.Certificate(certValue, "QUALIFIED");

        return new GetSidSession.Response(
            state,
            endResult,
            null, // signature
            cert,
            null  // interactionTypeUsed
        );
    }

    private static GetMidSession.Response midResponse(
        String state,
        String endResult,
        String certValue
    ) {
        return new GetMidSession.Response(state, endResult, certValue, null /* signature */);
    }
}
