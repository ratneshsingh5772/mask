package com.tokenization.mask.dto;

/** Wraps a raw JWT string for the decode endpoint. */
public record TokenRequest(String token) {
}
