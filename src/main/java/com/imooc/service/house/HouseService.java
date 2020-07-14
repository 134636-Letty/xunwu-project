package com.imooc.service.house;

import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.dto.HouseDTO;
import com.imooc.web.form.DatatableSearch;
import com.imooc.web.form.HouseForm;

public interface HouseService {
    ServiceResult<Boolean> save (HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);
}
