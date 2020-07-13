package com.imooc.service.house;

import com.imooc.dto.SupportAddressDTO;
import com.imooc.entity.SupportAddress;
import com.imooc.repository.HouseRepository;
import com.imooc.service.ServiceMultiResult;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    HouseRepository houseRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> getSupportCities(){
        List<SupportAddress> addresses = houseRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        List<SupportAddressDTO> result = new ArrayList<>();
        for (SupportAddress supportAddress : addresses){
            SupportAddressDTO target = modelMapper.map(supportAddress,SupportAddressDTO.class);
            result.add(target);
        }

        return new ServiceMultiResult<>(result.size(),result);
    }
}
