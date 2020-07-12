package com.imooc.security;

import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Map<String,String> authEntryPointMap;

    /**
     * @param loginFormUrl URL where the login page can be found. Should either be
     *                     relative to the web-app context path (include a leading {@code /}) or an absolute
     *                     URL.
     */
    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        authEntryPointMap = new HashMap<>();

        // 普通用户登录入口映射
        authEntryPointMap.put("/user/**", "/user/login");
        // 管理员登录入口映射
        authEntryPointMap.put("/admin/**", "/admin/login");
    }

    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception){
       String uri= request.getRequestURI().replace(request.getContextPath(),"");
       for (Map.Entry<String,String> authEntry :this.authEntryPointMap.entrySet()){
           if (this.pathMatcher.match(authEntry.getKey(),uri)){
               return authEntry.getValue();
           }
       }
       return super.determineUrlToUseForThisRequest(request,response,exception);
    }












}
