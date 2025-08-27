package com.friends.friends.Controller;

import com.friends.friends.Entity.Currency.CurrencyDto;
import com.friends.friends.Services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    
    @Autowired
    private CurrencyService currencyService;
    
    @GetMapping
    public ResponseEntity<List<CurrencyDto>> getAllCurrencies() {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }
}
