package com.friends.friends.Services;

import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Entity.Currency.CurrencyDto;
import com.friends.friends.Repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    
    @Autowired
    private CurrencyRepository currencyRepository;

    public List<CurrencyDto> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        return currencies.stream()
                .map(Currency::toDto)
                .collect(Collectors.toList());
    }
}
