package com.imooc.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "user") //映射数据库中的表 小写的
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 因为要同时兼容hibernate和h2所以不能用auto
    private Long id;

    private String name;
    private String password;
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private int status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_login_time")
    private Date lastLoginTime;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    private String avator;//头像
}
