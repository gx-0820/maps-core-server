package com.example.coreserver.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class UserLoginVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 4393557997355879737L;

    private Integer id;

    private String username;

    private String token;


    private String location;
    private String version;

}
