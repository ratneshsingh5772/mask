package com.tokenization.mask.dto;

import java.time.Instant;

/** Issued token plus its expiry, so clients know when they must request a new one. */
public record TokenResponse(String token, Instant expiresAt) {
}
