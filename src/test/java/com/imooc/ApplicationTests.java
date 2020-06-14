package com.imooc;

import com.imooc.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findById(){
//        Optional<User> user = userRepository.findById(1l);

        System.out.println("00000000000");
//        Assert.assertEquals("waliwali",user.get().getName());
    }

}
