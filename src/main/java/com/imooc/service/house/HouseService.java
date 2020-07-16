package com.imooc.service.house;

import com.imooc.base.ApiResponse;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.dto.HouseDTO;
import com.imooc.web.dto.HouseSubscribeDTO;
import com.imooc.web.form.DatatableSearch;
import com.imooc.web.form.HouseForm;
import com.imooc.web.form.RentSearch;
import org.springframework.data.util.Pair;

public interface HouseService {
    ServiceResult<Boolean> save (HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    ServiceResult<HouseDTO> getHouseInfoById(Long id);

    ServiceResult update(HouseForm houseForm);

    ServiceResult<Boolean> addTag(String tag,Long houseId);

    /**
     * 移除标签
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult removeTag(Long houseId, String tag);

    ServiceResult updateStatus(Long id,int status);

    /**
     * 管理员查询预约信息接口
     * @param start
     * @param size
     */
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);
}
