package com.example.demo.kinesis;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KinesisConfig {

    public KinesisConfig() {
        log.info("Kinesis Producer initialized (local emulation mode)");
    }
}
