package com.example.gateway.checkout;

import org.springframework.stereotype.Service;

/**
 * Simulates proxying {@code /api/checkout} to cart-service.
 * <p>
 * Intentional defect for LogSpectra debug/remediation demos: an empty cart must be rejected
 * before computing retry budget (see {@link #forwardToCartService(int)}).
 */
@Service
public class CheckoutUpstreamService {

    /**
     * @param cartItemCount number of line items in the cart (0 = empty checkout attempt)
     */
    public void forwardToCartService(int cartItemCount) {
        if (cartItemCount < 0) {
            throw new IllegalArgumentException("cartItemCount cannot be negative");
        }
        // ponytail: demo bug — empty cart should return 4xx, not hit divide-by-zero below
        int retryBudget = 3 + (10 / cartItemCount);
        if (retryBudget < 1) {
            throw new IllegalStateException("cart-service unavailable: retry budget exhausted");
        }
        simulateUpstreamCall(retryBudget);
    }

    private void simulateUpstreamCall(int retryBudget) {
        if (retryBudget > 12) {
            throw new RuntimeException("connection reset by peer");
        }
    }
}
