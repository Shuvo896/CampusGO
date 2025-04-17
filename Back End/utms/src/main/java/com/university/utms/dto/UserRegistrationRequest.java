package com.university.utms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String universityId;
    private String role;
}
