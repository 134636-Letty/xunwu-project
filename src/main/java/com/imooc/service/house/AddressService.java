package com.imooc.service.house;

import com.imooc.dto.SubwayDTO;
import com.imooc.dto.SubwayStationDTO;
import com.imooc.dto.SupportAddressDTO;
import com.imooc.service.ServiceMultiResult;

public interface AddressService {

    ServiceMultiResult<SupportAddressDTO> getSupportCities();

    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName);

    ServiceMultiResult<SubwayDTO> findAllSubwayByCity(String cityName);

    ServiceMultiResult<SubwayStationDTO> findAllStationBySubway(Long SubwayId);

    }
