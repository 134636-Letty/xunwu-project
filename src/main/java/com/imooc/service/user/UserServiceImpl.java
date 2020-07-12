package com.imooc.service.user;

import com.imooc.entity.Role;
import com.imooc.entity.User;
import com.imooc.repository.RoleRepository;
import com.imooc.repository.UserRepository;
import com.imooc.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @Override
    public User findUserByName(String userName) {
        User user = userRepository.findByName(userName);
        if (null == user){
            return null;
        }
       List<Role> roles = roleRepository.findRoleByUserId(user.getId());
        if (CollectionUtils.isEmpty(roles)){
          throw new DisabledException("权限非法！");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName())));

        user.setAuthorityList(authorities);
        return user;
    }
}
