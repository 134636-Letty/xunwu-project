package com.imooc;

import com.imooc.service.house.IQiNiuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class QiNiuTest extends ApplicationTests {

    @Autowired
    IQiNiuService qiNiuService;

    @Test
    public void Test(){
        String fileName="/Users/zhangjingyi/Downloads/project-myself/xunwu-project/tmp/tmp111111111.png";
        File file =new File(fileName);
        Assert.assertTrue(file.exists());
        try {
           Response response = qiNiuService.uploadFile(file);
           Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delete(){

        try {
            Response response = qiNiuService.delete("FnM3-Q2KXkQ4t0iUiu4wd4iD_6lI");
            Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
