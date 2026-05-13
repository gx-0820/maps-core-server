package com.example.coreserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PwdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oldPassword;

    private String newPassword;
}
