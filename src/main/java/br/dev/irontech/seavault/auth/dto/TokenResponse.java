package br.dev.irontech.seavault.auth.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
