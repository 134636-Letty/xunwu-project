package com.imooc.repository;

import com.imooc.entity.HouseSubscribe;
import org.springframework.data.repository.CrudRepository;

public interface HouseSubscribeRepository extends CrudRepository<HouseSubscribe,Long> {
    HouseSubscribe findByHouseIdAndUserId(Long houseId,Long userId);
}

