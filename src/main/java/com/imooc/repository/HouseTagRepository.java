package com.imooc.repository;

import com.imooc.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseTagRepository extends CrudRepository<HouseTag,Long> {
    List<HouseTag> findAllByHouseId(Long houseId);

    HouseTag findByHouseIdAndName(Long houseId,String name);
}
