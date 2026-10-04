package com.tripquest.trip;

import java.math.BigDecimal;
import java.util.Currency;

public sealed interface MoneyError permits MoneyError.NegativeAmount, MoneyError.ExcessivePrecision,
        MoneyError.CurrencyMismatch {
    String code();

    String message();

    record NegativeAmount(BigDecimal amount) implements MoneyError {
        public String code() { return "MONEY_NEGATIVE_AMOUNT"; }
        public String message() { return "Money amount must be positive or zero"; }
    }

    record ExcessivePrecision(BigDecimal amount, Currency currency) implements MoneyError {
        public String code() { return "MONEY_EXCESSIVE_PRECISION"; }
        public String message() { return "Amount has more decimal places than the currency allows"; }
    }

    record CurrencyMismatch(Currency expected, Currency actual) implements MoneyError {
        public String code() { return "MONEY_CURRENCY_MISMATCH"; }
        public String message() { return "Money currency does not match the trip currency"; }
    }
}
