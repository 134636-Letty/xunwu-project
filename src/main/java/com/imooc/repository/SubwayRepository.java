package com.imooc.repository;

import com.imooc.entity.Subway;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubwayRepository extends CrudRepository<Subway,Long> {
    List<Subway> findSubwaysByCityEnName(String cityEnName);
}
