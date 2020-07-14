package com.imooc.service;

import lombok.Data;

import java.util.List;

@Data
public class ServiceMultiResult<T> {
    private long total;
    private List<T> result;

    public ServiceMultiResult(long size, List<T> result) {
        this.total = size;
        this.result = result;
    }

    public int getResultSize(){
        if (this.result.size() ==0){
            return 0;
        }
        return this.result.size();
    }
}
