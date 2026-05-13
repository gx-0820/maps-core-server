package com.example.coreserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roleName;
    private String roleKey;
}
