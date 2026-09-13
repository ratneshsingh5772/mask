package com.tokenization.mask.dto;

import jakarta.validation.constraints.NotBlank;

/** Wraps a raw JWT string for endpoints that accept the token as a JSON field. */
public record TokenRequest(@NotBlank String token) {
}
