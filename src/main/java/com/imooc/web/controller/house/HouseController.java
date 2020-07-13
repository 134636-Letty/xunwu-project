package com.imooc.web.controller.house;

import com.imooc.base.ApiResponse;
import com.imooc.dto.SupportAddressDTO;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.house.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HouseController {
    @Autowired
    AddressService addressService;

    @GetMapping("/house/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result = addressService.getSupportCities();
        if (result.getResultSize() == 0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }
}
