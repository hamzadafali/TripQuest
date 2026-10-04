package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static DomainResult<Money, MoneyError> create(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            return DomainResult.rejected(new MoneyError.NegativeAmount(amount));
        }
        int scale = currency.getDefaultFractionDigits();
        if (scale < 0 || amount.scale() > scale) {
            return DomainResult.rejected(new MoneyError.ExcessivePrecision(amount, currency));
        }
        return DomainResult.success(new Money(amount.setScale(scale, RoundingMode.UNNECESSARY), currency));
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    public DomainResult<Money, MoneyError> add(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            return DomainResult.rejected(new MoneyError.CurrencyMismatch(currency, other.currency));
        }
        return DomainResult.success(new Money(amount.add(other.amount), currency));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Money that
                && amount.equals(that.amount) && currency.equals(that.currency);
    }

    @Override
    public int hashCode() { return Objects.hash(amount, currency); }

    @Override
    public String toString() { return amount + " " + currency.getCurrencyCode(); }
}
