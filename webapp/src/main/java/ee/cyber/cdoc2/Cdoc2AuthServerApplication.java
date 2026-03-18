package ee.cyber.cdoc2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public final class Cdoc2AuthServerApplication {
    private Cdoc2AuthServerApplication() {

    }

    public static void main(String[] args) {
        SpringApplication.run(Cdoc2AuthServerApplication.class, args);
    }
}
