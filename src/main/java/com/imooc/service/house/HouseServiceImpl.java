package com.imooc.service.house;

import com.google.common.collect.Maps;
import com.imooc.base.*;
import com.imooc.entity.*;
import com.imooc.repository.*;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.dto.HouseDTO;
import com.imooc.web.dto.HouseDetailDTO;
import com.imooc.web.dto.HousePictureDTO;
import com.imooc.web.dto.HouseSubscribeDTO;
import com.imooc.web.form.DatatableSearch;
import com.imooc.web.form.HouseForm;
import com.imooc.web.form.PhotoForm;
import com.imooc.web.form.RentSearch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.*;

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
    public ServiceResult<HouseDTO> getHouseInfoById(Long id){

        House house = houseRepository.findOne(id);
        if (null == house){
            return ServiceResult.notFound();
        }

        //detail
        HouseDetail detail = detailRepository.findAllByHouseId(house.getId());
        //tag
        List<HouseTag> tagList = tagRepository.findAllByHouseId(house.getId());
        //photo
        List<HousePicture> pictureList = pictureRepository.findAllByHouseId(house.getId());

        HouseDTO result = modelMapper.map(house,HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail,HouseDetailDTO.class);
        List<String> tagStrList = new ArrayList<>();
        tagList.forEach(tag->{tagStrList.add(tag.getName());});

        List<HousePictureDTO> pictureDTOList = new ArrayList<>();
        pictureList.forEach(picture->{pictureDTOList.add(modelMapper.map(picture, HousePictureDTO.class));});

        if (LoginUserUtil.getLoginUserId() >0){//已登录用户
            HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndUserId(house.getId(),LoginUserUtil.getLoginUserId());
            if (subscribe != null){
                result.setSubscribeStatus(subscribe.getStatus());
            }
        }

        //package
        result.setHouseDetail(houseDetailDTO);
        result.setTags(tagStrList);
        result.setPictures(pictureDTOList);

        return ServiceResult.of(result);
    }


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



    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        House house = this.houseRepository.findOne(houseForm.getId());
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = this.detailRepository.findAllByHouseId(house.getId());
        if (detail == null) {
            return ServiceResult.notFound();
        }

        ServiceResult wrapperResult = wrapperDetailInfo(detail, houseForm);
        if (wrapperResult != null) {
            return wrapperResult;
        }

        detailRepository.save(detail);

        List<HousePicture> pictures = generatePictures(houseForm, houseForm.getId());
        pictureRepository.save(pictures);

        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }

        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);

        /*if (house.getStatus() == HouseStatus.PASSES.getValue()) {
            searchService.index(house.getId());
        }*/

        return ServiceResult.success();
    }

    @Autowired
    SubwayStationRepository subwayStationRepository;
    @Autowired
    SubwayRepository subwayRepository;
    /**
     * 房源详细信息对象填充
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResult<>(false, "Not valid subway line!");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return new ServiceResult<>(false, "Not valid subway station!");
        }

        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;

    }


    /**
     * 图片对象列表信息填充
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    @Override
    public ServiceResult<Boolean> addTag(String tag,Long houseId) {
        House house = houseRepository.findOne(houseId);
        if (null == house){
            return ServiceResult.notFound();
        }

        HouseTag houseTag = tagRepository.findByHouseIdAndName(houseId, tag);
        if (null != houseTag) {
            return new ServiceResult(false, "标签已存在");
        }

        tagRepository.save(new HouseTag(houseId,tag));
        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = tagRepository.findByHouseIdAndName(houseId,tag);
        if (houseTag == null) {
            return new ServiceResult(false, "标签不存在");
        }

        tagRepository.delete(houseTag.getId());
        return ServiceResult.success();
    }


    @Override
    @Transactional
    public ServiceResult updateStatus(Long id,int status){
        House house = houseRepository.findOne(id);
        if (null == house){
            return ServiceResult.notFound();
        }
        if (house.getStatus() == status){
            return new ServiceResult(false,"状态没有变化！");
        }
        if (house.getStatus()==HouseStatus.DELETED.getValue() ){
            return new ServiceResult(false,"已删除的房源不可修改状态！");

        }
        if (house.getStatus()==HouseStatus.RENTED.getValue() ){
            return new ServiceResult(false,"已出租的房源不可修改状态！");

        }
        houseRepository.updateStatus(id,status);
        return ServiceResult.success();
    }



    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        Pageable pageable = new PageRequest(start / size, size, new Sort(Sort.Direction.DESC, "orderTime"));

        Page<HouseSubscribe> page = subscribeRepository.findAllByAdminIdAndStatus(userId, HouseSubscribeStatus.IN_ORDER_TIME.getValue(), pageable);

        return wrapper(page);
    }


    // todo  copy 来的
    private ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> wrapper(Page<HouseSubscribe> page) {
        List<Pair<HouseDTO, HouseSubscribeDTO>> result = new ArrayList<>();

        if (page.getSize() < 1) {
            return new ServiceMultiResult<>(page.getTotalElements(), result);
        }

        List<HouseSubscribeDTO> subscribeDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        page.forEach(houseSubscribe -> {
            subscribeDTOS.add(modelMapper.map(houseSubscribe, HouseSubscribeDTO.class));
            houseIds.add(houseSubscribe.getHouseId());
        });

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseRepository.findAll(houseIds);
        houses.forEach(house -> {
            idToHouseMap.put(house.getId(), modelMapper.map(house, HouseDTO.class));
        });

        for (HouseSubscribeDTO subscribeDTO : subscribeDTOS) {
            Pair<HouseDTO, HouseSubscribeDTO> pair = Pair.of(idToHouseMap.get(subscribeDTO.getHouseId()), subscribeDTO);
            result.add(pair);
        }

        return new ServiceMultiResult<>(page.getTotalElements(), result);
    }


    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture cover = pictureRepository.findOne(coverId);
        if (cover == null) {
            return ServiceResult.notFound();
        }

        houseRepository.updateCover(targetId, cover.getPath());
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        return simpleQuery(rentSearch);
    }
    public ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(),rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();
        Pageable pageable = new PageRequest(page,rentSearch.getSize(),sort);
        Specification<House> specification = ((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"),HouseStatus.PASSES.getValue());

            predicate = criteriaBuilder.and(predicate,criteriaBuilder.equal(root.get("cityEnName"),rentSearch.getCityEnName()));

            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())){
                predicate = criteriaBuilder.and(predicate,criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY),-1)); //todo ??? why -1
            }
            return predicate;
        });

        Page<House> houses = houseRepository.findAll(specification,pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();

        List<Long> houseIds = new ArrayList<>();
        Map<Long,HouseDTO> idToHouseMap = Maps.newHashMap();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house,HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix+houseDTO.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(),houseDTO);
        });

        wrapperHouseList(houseIds,idToHouseMap);

        return new ServiceMultiResult<>(houses.getTotalElements(),houseDTOS);
    }

    public void wrapperHouseList(List<Long> houseIds,Map<Long,HouseDTO> idToHouseMap){
        List<HouseDetail> detailList = detailRepository.findByHouseIdIn(houseIds);
        detailList.forEach(houseDetail -> {
            HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail,HouseDetailDTO.class);
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            houseDTO.setHouseDetail(houseDetailDTO);

        });

        List<HouseTag> houseTags = tagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(tag->{
           HouseDTO houseDTO = idToHouseMap.get(tag.getHouseId());
           houseDTO.getTags().add(tag.getName());
        });

    }




}
