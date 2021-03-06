package com.imooc.repository;

import com.imooc.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseDetailRepository extends CrudRepository<HouseDetail,Long> {

    HouseDetail findAllByHouseId(Long houseId);

    List<HouseDetail> findByHouseIdIn(List<Long> houseIds);
}
