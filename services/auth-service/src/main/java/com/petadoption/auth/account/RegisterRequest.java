package com.petadoption.auth.account;

public record RegisterRequest(String email, String password, String role) {}
