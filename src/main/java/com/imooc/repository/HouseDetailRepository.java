package com.imooc.repository;

import com.imooc.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

public interface HouseDetailRepository extends CrudRepository<HouseDetail,Long> {

    HouseDetail findAllByHouseId(Long houseId);
}
