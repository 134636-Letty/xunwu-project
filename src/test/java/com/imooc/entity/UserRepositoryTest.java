package com.imooc.entity;

import com.imooc.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest
@Configuration
@ActiveProfiles("test") //h2数据库
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findOne(){
       User user = userRepository.findOne(1l);

        System.out.println(user);
//        Assert.assertEquals("waliwali",user.get().getName());
    }
}
