package com.imooc.service.house;

import com.imooc.dto.SubwayDTO;
import com.imooc.dto.SubwayStationDTO;
import com.imooc.dto.SupportAddressDTO;
import com.imooc.entity.Subway;
import com.imooc.entity.SubwayStation;
import com.imooc.entity.SupportAddress;
import com.imooc.repository.HouseRepository;
import com.imooc.repository.SubwayRepository;
import com.imooc.repository.SubwayStationRepository;
import com.imooc.service.ServiceMultiResult;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    HouseRepository houseRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    SubwayRepository subwayRepository;
    @Autowired
    SubwayStationRepository subwayStationRepository;

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

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName){
        if (StringUtils.isEmpty(cityName)){
            return new ServiceMultiResult<>(0,null);
        }

        List<SupportAddress> regions = houseRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION.getValue(),cityName);
        List<SupportAddressDTO> result = new ArrayList<>();
        for (SupportAddress region : regions){
            SupportAddressDTO target = modelMapper.map(region,SupportAddressDTO.class);
            result.add(target);
        }

        return new ServiceMultiResult<>(result.size(),result);
    }

    public ServiceMultiResult<SubwayDTO> findAllSubwayByCity(String cityName){
        List<Subway> subways =  subwayRepository.findSubwaysByCityEnName(cityName);
        List<SubwayDTO> result = new ArrayList<>();
        subways.forEach(subway -> result.add(modelMapper.map(subway,SubwayDTO.class)));
        return new ServiceMultiResult<>(result.size(),result);
    }

    public ServiceMultiResult<SubwayStationDTO> findAllStationBySubway(Long SubwayId){
        List<SubwayStation> subwayStations =  subwayStationRepository.findSubwayStationsBySubwayId(SubwayId);
        List<SubwayStationDTO> result = new ArrayList<>();
        subwayStations.forEach(subwayStation -> result.add(modelMapper.map(subwayStation,SubwayStationDTO.class)));
        return new ServiceMultiResult<>(result.size(),result);
    }
}
