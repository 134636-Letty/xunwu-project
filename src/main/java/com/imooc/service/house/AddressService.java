package com.imooc.service.house;

import com.imooc.dto.SupportAddressDTO;
import com.imooc.service.ServiceMultiResult;

public interface AddressService {

    ServiceMultiResult<SupportAddressDTO> getSupportCities();
}
