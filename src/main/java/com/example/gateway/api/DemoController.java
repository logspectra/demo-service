package com.example.gateway.api;

import com.example.gateway.checkout.CheckoutUpstreamService;
import com.example.gateway.demo.DemoErrorBurstService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final DemoErrorBurstService errorBurstService;
    private final CheckoutUpstreamService checkoutUpstreamService;

    public DemoController(DemoErrorBurstService errorBurstService, CheckoutUpstreamService checkoutUpstreamService) {
        this.errorBurstService = errorBurstService;
        this.checkoutUpstreamService = checkoutUpstreamService;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(@RequestParam(defaultValue = "world") String name) {
        log.info("Hello request received for name={}", name);
        return ResponseEntity.ok(Map.of("message", "Hello, " + name));
    }

    /**
     * Triggers the same checkout proxy path as scheduled demo errors (empty cart → defect).
     */
    @GetMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @RequestParam(defaultValue = "0") int cartItems) {
        try {
            checkoutUpstreamService.forwardToCartService(cartItems);
            return ResponseEntity.ok(Map.of("route", "/api/checkout", "status", 200, "cartItems", cartItems));
        } catch (Exception ex) {
            log.error(
                    "gateway upstream failure route=/api/checkout status=502 upstream=cart-service cartItems={}",
                    cartItems,
                    ex);
            return ResponseEntity.status(502).body(Map.of(
                    "route", "/api/checkout",
                    "status", 502,
                    "cartItems", cartItems,
                    "error", ex.getClass().getSimpleName()));
        }
    }

    @GetMapping("/error-demo")
    public ResponseEntity<Void> errorDemo() {
        log.warn("About to throw a demo exception for LogSpectra UI verification");
        throw new IllegalStateException("Intentional demo error — should appear in LogSpectra with stack trace");
    }

    /**
     * Emit {@code count} ERROR log lines (no HTTP 500) for live dashboard / SSE testing.
     */
    @GetMapping("/error-burst")
    public ResponseEntity<Map<String, Object>> errorBurstGet(@RequestParam(defaultValue = "10") int count) {
        return errorBurst(count);
    }

    @PostMapping("/error-burst")
    public ResponseEntity<Map<String, Object>> errorBurstPost(@RequestParam(defaultValue = "10") int count) {
        return errorBurst(count);
    }

    private ResponseEntity<Map<String, Object>> errorBurst(int count) {
        int emitted = errorBurstService.emitBurst(count);
        return ResponseEntity.ok(Map.of("emitted", emitted, "level", "ERROR"));
    }
}
