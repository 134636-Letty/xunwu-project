package com.imooc;

import com.imooc.entity.User;
import com.imooc.repository.UserRepository;
import com.imooc.service.IUserService;
import com.imooc.service.user.UserServiceImpl;
import com.qiniu.util.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Configuration
@ActiveProfiles("test")
public class ApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findById(){
//        Optional<User> user = userRepository.findById(1l);

        System.out.println("00000000000");
//        Assert.assertEquals("waliwali",user.get().getName());
    }

    @Autowired
    IUserService userService;

    @Test
    public void Test(){
        User user = userService.findUserByName("admin");
        System.out.println(user);
    }

}
