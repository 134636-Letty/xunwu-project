package com.imooc.service.house;


import com.imooc.entity.SupportAddress;
import com.imooc.service.ServiceMultiResult;
import com.imooc.web.dto.SubwayDTO;
import com.imooc.web.dto.SubwayStationDTO;
import com.imooc.web.dto.SupportAddressDTO;

import java.util.Map;

public interface AddressService {

    ServiceMultiResult<SupportAddressDTO> getSupportCities();

    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName);

    ServiceMultiResult<SubwayDTO> findAllSubwayByCity(String cityName);

    ServiceMultiResult<SubwayStationDTO> findAllStationBySubway(Long SubwayId);

    Map<SupportAddress.Level,SupportAddressDTO> findCityAndRegion(String cityName, String regionEnName);

    }
