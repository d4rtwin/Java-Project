package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminContactRes {
    private Long userId;
    private String fullName;
    private String avatarUrl;
}