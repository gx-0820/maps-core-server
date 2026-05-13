package com.example.coreserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RPDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roleId;
    private int permissionId;
}
