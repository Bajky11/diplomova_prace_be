package com.friends.friends.Services;

import com.friends.friends.Entity.Account.*;
import com.friends.friends.Exception.Account.AccountAlreadyExistsException;
import com.friends.friends.Exception.Auth.InvalidCredentialsException;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    private AresService aresService = new AresService();

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtResetTokenService jwtResetTokenService;

    public LoginResponse register(AccountRegisterDto registerDto) {
        boolean emailExists = accountRepository.findByEmail(registerDto.getEmail()).isPresent();
        boolean icoExists = StringUtils.hasText(registerDto.getIco()) && accountRepository.existsByIco((registerDto.getIco().trim()));

        if(icoExists){
            throw new AccountAlreadyExistsException("Ico already exists");
        }
        if (emailExists) {
            throw new AccountAlreadyExistsException("Email already exists");
        }

        Account user = Account.builder()
                .imageUrl(registerDto.getImageUrl())
                .email(registerDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .name(registerDto.getName())
                .isBusiness(registerDto.getIsBusiness())
                .region(registerDto.getRegion())
                .build();

        if (registerDto.getIco() != null) {
            boolean result = aresService.checkIfIcoIsValid(registerDto.getIco().trim());
            if (!result) {
                throw new InvalidCredentialsException("Submited ico is not valid");
            }
            user.setIco(registerDto.getIco().trim());
        }

        Account savedUser = accountRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new LoginResponse(token, savedUser.toDto());
    }

    public LoginResponse login(AccountLoginDto loginDto) {
        Optional<Account> userOpt = accountRepository.findByEmail(loginDto.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(loginDto.getPassword(), userOpt.get().getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Account user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(token, user.toDto());
    }

    public void resetPassword(String token, String newPassword) {
        long userId = jwtResetTokenService.verifyAndGetUserId(token);
        Account user = accountService.getAccountById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(user);
    }
}
