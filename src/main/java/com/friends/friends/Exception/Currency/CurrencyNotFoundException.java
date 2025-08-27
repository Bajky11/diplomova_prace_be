package com.friends.friends.Exception.Currency;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException() {
        super("Currency not found");
    }
}
