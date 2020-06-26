package com.imooc.config;

import com.imooc.security.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * HTTP权限控制
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //资源访问权限
        http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() //管理员登录入口
        .antMatchers("/static/**").permitAll()//资源，运行任何权限可以访问静态资源
        .antMatchers("/user/login").permitAll()
        .antMatchers("/admin/**").hasRole("ADMIN")//admin开头的需要管理员权限
        .antMatchers("/user/**").hasAnyRole("ADMIN","USER") //user开头的资源需要管理员或普通用户权限
        .and()
        .formLogin()
        .loginProcessingUrl("/login") //配置角色登录处理入口
        .and();

        http.csrf().disable();//防御策略
        http.headers().frameOptions().sameOrigin();//同源策略
    }


    /**
     * 自定义认证策略
     * 一个类里只能注入一个AuthenticationManagerBuilder 不然会产生不可预估的影响
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider()).eraseCredentials(true);
    }

    @Bean
    public AuthProvider authProvider(){
        return new AuthProvider();
    }


    @Autowired
    public void configGlobal1(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN").and();
    }
}
