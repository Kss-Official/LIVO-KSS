package com.livo.api.engines.pattern;

import org.springframework.stereotype.Component;

/**
 * Exponential Moving Average (EMA) Engine.
 * O(1) memory dynamic smoothed tracking of user productivity velocity.
 * Formula: EMA_t = (alpha * value) + ((1 - alpha) * EMA_{t-1})
 */
@Component
public class ExponentialMovingAverage {

    private static final double DEFAULT_ALPHA = 0.2; // 20% weight to newest observation

    public double update(double previousEma, double currentObservation, double alpha) {
        double effectiveAlpha = (alpha > 0.0 && alpha <= 1.0) ? alpha : DEFAULT_ALPHA;
        double updated = (effectiveAlpha * currentObservation) + ((1.0 - effectiveAlpha) * previousEma);
        return Math.round(updated * 100.0) / 100.0;
    }

    public double update(double previousEma, double currentObservation) {
        return update(previousEma, currentObservation, DEFAULT_ALPHA);
    }
}
