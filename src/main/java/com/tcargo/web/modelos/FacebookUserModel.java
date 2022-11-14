package com.tcargo.web.modelos;

import lombok.Data;

import java.io.Serializable;

@Data
public class FacebookUserModel implements Serializable {

    private String id;
    private String name;
    private String email;
    private String accessToken;

}
