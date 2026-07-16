package com.bank.banking_api.domain;


import java.util.UUID;

public interface UserInfo {
    UUID getUserId();
    String getUsername();
    String getPassword();
    String getRole();
}