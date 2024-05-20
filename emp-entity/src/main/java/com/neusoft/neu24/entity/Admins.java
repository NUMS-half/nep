package com.neusoft.neu24.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admins {
    private  String admin_id;
    private  String admin_code;
    private String password;
    private  String  remarks;
}
