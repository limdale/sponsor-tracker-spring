package com.limdale.sponsortracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private AppUser user;
    private String token;
}
