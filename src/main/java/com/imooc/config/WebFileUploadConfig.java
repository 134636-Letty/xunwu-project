package com.imooc.config;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;


import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

@Configuration
@ConditionalOnClass({Servlet.class, StandardServletMultipartResolver.class})
@ConditionalOnProperty(prefix = "spring.http.multipart",name = "enabled",matchIfMissing = true)
public class WebFileUploadConfig {
    private final MultipartProperties multipartProperties;

    public WebFileUploadConfig(MultipartProperties multipartProperties) {
        this.multipartProperties = multipartProperties;
    }

    /**
     * 上传配置
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public MultipartConfigElement multipartConfigElement(){
        return this.multipartProperties.createMultipartConfig();
    }

    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(MultipartResolver.class)
    public StandardServletMultipartResolver multipartResolver(){
        StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
        return multipartResolver;
    }

    /**
     * 华南机房
     * @return
     */
    @Bean
    public com.qiniu.storage.Configuration qiniuConfig(){
        return new com.qiniu.storage.Configuration(Region.region2());
    }

    @Bean
    public UploadManager uploadManager(){
        return new UploadManager(qiniuConfig());
    }

    @Value("${qiniu.AccessKey}")
    private String accessKey;

    @Value("${qiniu.SecretKey}")
    private String secretKey;

    /**
     * 认证信息实例
     * @return
     */
    @Bean
    public Auth auth(){
        return Auth.create(accessKey,secretKey);
    }

    /**
     * 构建七牛空间管理实例
     * @return
     */
    @Bean
    public BucketManager bucketManager(){
        return new BucketManager(auth(),qiniuConfig());
    }
}
