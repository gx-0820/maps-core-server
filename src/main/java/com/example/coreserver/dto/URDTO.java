package com.example.coreserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class URDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int userId;
    private int roleId;
}
