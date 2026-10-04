package com.tripquest.trip;

import java.util.Objects;

public record IndicativeTotal(Money knownTotal, boolean hasMissingPrices) {
    public IndicativeTotal {
        Objects.requireNonNull(knownTotal, "knownTotal");
    }
}
