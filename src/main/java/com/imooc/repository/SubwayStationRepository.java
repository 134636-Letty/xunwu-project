package com.imooc.repository;

import com.imooc.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubwayStationRepository extends CrudRepository<SubwayStation,Long> {
    List<SubwayStation> findSubwayStationsBySubwayId(Long subwayId);
}
