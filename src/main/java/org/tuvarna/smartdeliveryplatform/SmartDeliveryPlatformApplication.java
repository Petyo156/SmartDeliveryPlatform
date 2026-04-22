package org.tuvarna.smartdeliveryplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SmartDeliveryPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDeliveryPlatformApplication.class, args);
    }

}
