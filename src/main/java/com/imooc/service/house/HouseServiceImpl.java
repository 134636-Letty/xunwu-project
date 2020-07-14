package com.imooc.service.house;

import com.imooc.base.HouseStatus;
import com.imooc.base.LoginUserUtil;
import com.imooc.entity.House;
import com.imooc.entity.HouseDetail;
import com.imooc.entity.HousePicture;
import com.imooc.entity.HouseTag;
import com.imooc.repository.*;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.dto.HouseDTO;
import com.imooc.web.form.DatatableSearch;
import com.imooc.web.form.HouseForm;
import com.imooc.web.form.PhotoForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    HouseRepository houseRepository;
    @Autowired
    HouseDetailRepository detailRepository;
    @Autowired
    HousePictureRepository pictureRepository;
    @Autowired
    HouseSubscribeRepository subscribeRepository;
    @Autowired
    HouseTagRepository tagRepository;
    @Autowired
    ModelMapper modelMapper;
    @Value("${qiniu.cdn.prefix}")
    String cdnPrefix;

    @Override
    @Transactional
    public ServiceResult<Boolean> save (HouseForm houseForm){

        House house = modelMapper.map(houseForm,House.class);
        house.setCreateTime(new Date());
        house.setLastUpdateTime(new Date());
        house.setAdminId(LoginUserUtil.getLoginUserId());
        houseRepository.save(house);

        HouseDetail detail = new HouseDetail();
        modelMapper.map(houseForm,detail);
        detail.setHouseId(house.getId());
        detailRepository.save(detail);


        if (!CollectionUtils.isEmpty(houseForm.getTags())){
            List<HouseTag> tagList = getTags(houseForm,house.getId());
            tagRepository.save(tagList);
        }

        if (!CollectionUtils.isEmpty(houseForm.getPhotos())){
            List<HousePicture> pictures = getPhoto(houseForm,house.getId());
            pictureRepository.save(pictures);
        }

        return new ServiceResult<Boolean>(true);

    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody){
        List<HouseDTO> houseDTOS = new ArrayList<>();

        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()),searchBody.getOrderBy());
        int page = searchBody.getStart() / searchBody.getLength();
        Pageable pageable = new PageRequest(page,searchBody.getLength(),sort);
        Specification<House> specification = ((root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"),LoginUserUtil.getLoginUserId());
            predicate = cb.and(predicate,cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));
            if (searchBody.getCity() != null){
                predicate = cb.and(predicate,cb.equal(root.get("cityEnName"),searchBody.getCity()));
            }

            if (searchBody.getStatus() != null){
                predicate = cb.and(predicate,cb.equal(root.get("status"),searchBody.getStatus()));
            }

            if (searchBody.getCreateTimeMin() != null){
                predicate = cb.and(predicate,cb.greaterThanOrEqualTo(root.get("createTime"),searchBody.getCreateTimeMin()));
            }

            if (searchBody.getCreateTimeMax() != null){
                predicate = cb.and(predicate,cb.lessThanOrEqualTo(root.get("createTime"),searchBody.getCreateTimeMax()));
            }

            if (searchBody.getTitle() != null){
                predicate = cb.and(predicate,cb.like(root.get("title"),"%"+searchBody.getTitle() +"%"));
            }
            return predicate;

        });

        Page<House> houses = houseRepository.findAll(specification,pageable);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house,HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }


    public List<HouseTag> getTags(HouseForm houseForm,Long houseId){
        List<HouseTag> tagList = new ArrayList<>();
        houseForm.getTags().forEach(tag->tagList.add(new HouseTag(houseId,tag)));
        return tagList;
    }


    public List<HousePicture> getPhoto(HouseForm houseForm,Long houseId){
        List<HousePicture> housePictures = new ArrayList<>();
        for (PhotoForm photoForm :houseForm.getPhotos()){
            HousePicture housePicture = new HousePicture();
            housePicture.setHouseId(houseId);
            housePicture.setHeight(photoForm.getHeight());
            housePicture.setWidth(photoForm.getWidth());
            housePicture.setCdnPrefix(cdnPrefix);
            housePicture.setPath(photoForm.getPath());
            housePictures.add(housePicture);
        }
        return housePictures;
    }





}
