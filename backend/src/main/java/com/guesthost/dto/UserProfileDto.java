package com.guesthost.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private String name;
    private String iban;
    private String swift;
}
