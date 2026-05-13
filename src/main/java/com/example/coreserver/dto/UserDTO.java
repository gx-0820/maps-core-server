package com.example.coreserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8347822700891152077L;

    @NotBlank(message = "账号不能为空")
//    @Pattern(regexp = "^\\w{5,20}$", message = "用户名的长度必须为5~16位")
    private String username; // 账号

    @NotBlank(message = "密码不能为空")
//    @Pattern(regexp = "^\\w{5,16}$", message = "密码的长度必须为5~16位")
    private String password; // 密码

}
