package com.example.loganalysis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);
    private final Random random = new Random();

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        log.info("Success endpoint called");
        return ResponseEntity.ok("Success!");
    }

    @GetMapping("/slow")
    public ResponseEntity<String> slow(@RequestParam(defaultValue = "2000") long delayMs)
            throws InterruptedException {
        log.info("Slow endpoint called, delaying {}ms", delayMs);
        Thread.sleep(delayMs);
        log.info("Slow endpoint completed");
        return ResponseEntity.ok("Slow response after " + delayMs + "ms");
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        log.error("Error endpoint called - simulating error");
        throw new RuntimeException("Simulated error for testing");
    }

    @GetMapping("/random")
    public ResponseEntity<String> random() throws InterruptedException {
        int scenario = random.nextInt(10);

        if (scenario < 7) {
            log.info("Random endpoint - success scenario");
            return ResponseEntity.ok("Success");
        } else if (scenario < 9) {
            int delay = 1000 + random.nextInt(2000);
            log.warn("Random endpoint - slow scenario, delay {}ms", delay);
            Thread.sleep(delay);
            return ResponseEntity.ok("Slow success after " + delay + "ms");
        } else {
            log.error("Random endpoint - error scenario");
            throw new RuntimeException("Random error occurred");
        }
    }

    @GetMapping("/generate-logs")
    public ResponseEntity<String> generateLogs(@RequestParam(defaultValue = "100") int count)
            throws InterruptedException {
        log.info("Generating {} sample logs", count);

        for (int i = 0; i < count; i++) {
            int type = random.nextInt(100);

            if (type < 70) {
                log.info("Sample log {} - normal operation", i);
            } else if (type < 85) {
                log.debug("Sample log {} - debug info", i);
            } else if (type < 95) {
                log.warn("Sample log {} - warning condition", i);
            } else {
                log.error("Sample log {} - error occurred", i);
            }

            if (i % 10 == 0) {
                Thread.sleep(10);
            }
        }

        log.info("Finished generating {} logs", count);
        return ResponseEntity.ok("Generated " + count + " sample logs");
    }
}
