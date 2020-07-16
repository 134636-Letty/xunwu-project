package com.imooc.service;

import com.imooc.entity.User;
import com.imooc.web.dto.UserDTO;

public interface IUserService {
    User findUserByName(String userName);

    ServiceResult<UserDTO> findById(Long userId);
}
