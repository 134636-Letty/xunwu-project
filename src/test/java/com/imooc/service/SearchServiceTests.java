package com.imooc.service;

import com.imooc.Application;
import com.imooc.ApplicationTests;
import com.imooc.service.search.ISearchService;
import org.elasticsearch.search.SearchService;
import org.junit.Assert;
import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceTests extends ApplicationTests {

    @Autowired
    ISearchService searchService;

    @Test
    public void test(){

       Long houseId = 15L;
         searchService.index(houseId);
//        Assert.assertTrue(success);
    }

    @Test
    public void rmove(){
        Long houseId = 15L;
        searchService.remove(houseId);
    }
}
