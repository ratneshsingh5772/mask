package com.tokenization.mask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * The business payload of this API. Normally this would be a JSON request body;
 * here it travels as the claim set of a signed JWT instead.
 */
public record UserPayload(

        @NotNull @Positive
        Long userId,

        @NotBlank
        String name,

        @NotBlank
        @Pattern(regexp = "ADMIN|MANAGER|USER", message = "must be one of ADMIN, MANAGER, USER")
        String role) {
}
