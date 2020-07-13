package com.imooc.repository;

import com.imooc.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseRepository extends CrudRepository<SupportAddress,Long> {

    List<SupportAddress> findAllByLevel (String level);
}
