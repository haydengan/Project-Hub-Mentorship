package com.togetherly.demo.data.group.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 500) String content) {}
