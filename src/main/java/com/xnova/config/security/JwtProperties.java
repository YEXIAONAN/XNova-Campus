package com.xnova.config.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "xnova.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @Min(300)
    private long accessExpireSeconds;
}
