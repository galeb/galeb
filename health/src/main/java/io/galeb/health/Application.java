/**
 *
 */

package io.galeb.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public final static String PREFIX = System.getProperty("APP_PREFIX", "PLANC");

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
