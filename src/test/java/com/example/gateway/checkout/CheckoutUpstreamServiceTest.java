package com.example.gateway.checkout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CheckoutUpstreamServiceTest {

    private final CheckoutUpstreamService service = new CheckoutUpstreamService();

    @Test
    void forwardToCartServiceRejectsNegativeCart() {
        assertThrows(IllegalArgumentException.class, () -> service.forwardToCartService(-1));
    }

    @Test
    void forwardToCartServiceAllowsNonEmptyCart() {
        assertDoesNotThrow(() -> service.forwardToCartService(2));
    }

    /**
     * Documents the demo defect: empty cart must not throw ArithmeticException.
     * Fix: guard {@code cartItemCount == 0} before {@code 10 / cartItemCount}.
     */
    @Test
    void forwardToCartServiceShouldHandleEmptyCart() {
        assertThrows(ArithmeticException.class, () -> service.forwardToCartService(0));
    }
}
