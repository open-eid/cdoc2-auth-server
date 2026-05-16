package ee.cyber.cdoc2.server.app.usecase.status;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;

@Component
@RequiredArgsConstructor
public class JwtIssuanceClaimsCreator {
    private final Clock clock;

    public JWTClaimsSet execute(Map<String, Object> claimsMap) throws ParseException {
        Instant now = clock.instant();

        JWTClaimsSet issuanceClaims = new JWTClaimsSet.Builder()
            .issueTime(Date.from(now))
            .expirationTime(
                Date.from(now.plus(1, ChronoUnit.DAYS))
            )
            .build();
        Map<String, Object> issuanceClaimsMap = issuanceClaims.toJSONObject();

        claimsMap.putAll(issuanceClaimsMap);

        return JWTClaimsSet.parse(claimsMap);
    }
}
