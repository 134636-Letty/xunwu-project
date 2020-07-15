package com.imooc.service.house;

import com.imooc.entity.Subway;
import com.imooc.entity.SubwayStation;
import com.imooc.entity.SupportAddress;
import com.imooc.repository.HouseRepository;
import com.imooc.repository.SubwayRepository;
import com.imooc.repository.SubwayStationRepository;
import com.imooc.repository.SupportAddressRepository;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.dto.SubwayDTO;
import com.imooc.web.dto.SubwayStationDTO;
import com.imooc.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    SupportAddressRepository supportAddressRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    SubwayRepository subwayRepository;
    @Autowired
    SubwayStationRepository subwayStationRepository;

    @Override
    public ServiceMultiResult<SupportAddressDTO> getSupportCities(){
        List<SupportAddress> addresses = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
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

        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION.getValue(),cityName);
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

    @Override
    public Map<SupportAddress.Level,SupportAddressDTO> findCityAndRegion(String cityName,String regionEnName){

        Map<SupportAddress.Level,SupportAddressDTO> result = new HashMap<>();

        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityName,SupportAddress.Level.CITY.getValue());

        SupportAddress region =supportAddressRepository.findByEnNameAndBelongTo(regionEnName,cityName);

        result.put(SupportAddress.Level.CITY,modelMapper.map(city,SupportAddressDTO.class));
        result.put(SupportAddress.Level.REGION,modelMapper.map(region,SupportAddressDTO.class));
        return result;
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Long subWagId){
       if (null == subWagId){
           return ServiceResult.notFound();
       }
       Subway subway = subwayRepository.findOne(subWagId);
       if (null == subway){
           return ServiceResult.notFound();
       }
       SubwayDTO subwayDTO =modelMapper.map(subway,SubwayDTO.class);
       return ServiceResult.of(subwayDTO);
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Long subwayStationId){
        if (null == subwayStationId){
            return ServiceResult.notFound();
        }
        SubwayStation subwayStation = subwayStationRepository.findOne(subwayStationId);
        if (null == subwayStation){
            return ServiceResult.notFound();
        }
        SubwayStationDTO subwayStationDTO = modelMapper.map(subwayStation,SubwayStationDTO.class);
        return ServiceResult.of(subwayStationDTO);
    }










}
