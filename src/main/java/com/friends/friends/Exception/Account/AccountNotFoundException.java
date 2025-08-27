package com.friends.friends.Exception.Account;

public class AccountNotFoundException extends RuntimeException {
 public AccountNotFoundException() {
     super("Account not found");
 }
}
