package com.imooc.web.controller.admin;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.imooc.base.ApiDataTableResponse;
import com.imooc.base.ApiResponse;
import com.imooc.base.HouseOperation;
import com.imooc.base.HouseStatus;
import com.imooc.entity.HouseDetail;
import com.imooc.entity.SupportAddress;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.service.house.AddressService;
import com.imooc.service.house.HouseService;
import com.imooc.service.house.IQiNiuService;
import com.imooc.web.dto.*;
import com.imooc.web.form.DatatableSearch;
import com.imooc.web.form.HouseForm;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    IQiNiuService qiNiuService;
    @Autowired
    Gson gson;
    @Autowired
    HouseService houseService;
    @Autowired
    AddressService addressService;

    @GetMapping("/admin/center")
    public String adminCenterPage(){
        return "admin/center";
    }

    @GetMapping("/admin/welcome")
    public String welComePage(){
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "/admin/login";
    }

    /**
     * 新增房源功能页
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    /**
     * 新增房源功能页
     * @return
     */
    @PostMapping("admin/add/houseInfo")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add")HouseForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),bindingResult.getAllErrors().get(0).getDefaultMessage(),null);
        }

        if (form.getPhotos() == null || form.getCover() == null){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),"必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> map = addressService.findCityAndRegion(form.getCityEnName(),form.getRegionEnName());
        if (map.keySet().size() !=2){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<Boolean> result = houseService.save(form);
        if (result.isSuccess()){
            return ApiResponse.ofSuccess(result.getResult());
        }

        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
    }

    @GetMapping("admin/house/list")
    public String houseListPage(){
        return "admin/house-list";
    }

    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody){
        ServiceMultiResult<HouseDTO> result =houseService.adminQuery(searchBody);
        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;
    }

    @GetMapping("admin/house/edit")
    public String getHouseInfoById(@RequestParam(value = "id") Long id, Model model){
        if (null == id || id<1){
            return  "404";
        }

        ServiceResult<HouseDTO> result = houseService.getHouseInfoById(id);
        if (!result.isSuccess()){
            return "404";
        }
        HouseDTO houseDTO =result.getResult();
        model.addAttribute("house",result.getResult());

        Map<SupportAddress.Level,SupportAddressDTO>  map =addressService.findCityAndRegion(houseDTO.getCityEnName(),houseDTO.getRegionEnName());
        model.addAttribute("city",map.get(SupportAddress.Level.CITY));
        model.addAttribute("region",map.get(SupportAddress.Level.REGION));

        HouseDetailDTO detailDTO = houseDTO.getHouseDetail();
        ServiceResult<SubwayDTO> subwayDTOServiceResult = addressService.findSubway(detailDTO.getSubwayLineId());
        if (subwayDTOServiceResult.isSuccess()) {
            model.addAttribute("subway", subwayDTOServiceResult.getResult());
        }

        ServiceResult<SubwayStationDTO> stationDTOServiceResult =  addressService.findSubwayStation(detailDTO.getSubwayStationId());
        if (stationDTOServiceResult.isSuccess()) {
            model.addAttribute("station", stationDTOServiceResult.getResult());
        }

        return "admin/house-edit";
    }



    @PostMapping("admin/house/edit")
    @ResponseBody
    public ApiResponse saveHouse(@Valid @ModelAttribute("form-house-edit")HouseForm houseForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }

        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());

        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult result = houseService.update(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }

        ApiResponse response = ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        response.setMessage(result.getMessage());
        return response;
    }




    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file")MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String fileName = file.getOriginalFilename();
       /* File target = new File("/Users/zhangjingyi/Downloads/project-myself/xunwu-project/tmp"+fileName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
*/

        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiNiuService.uploadFile(inputStream);
            if (response.isOK()) {
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            } else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        }catch (QiniuException e){
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode,response.bodyString());
            } catch (QiniuException e1) {
                 e1.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }

        }catch (IOException e) {
                e.printStackTrace();
            }

            return ApiResponse.ofSuccess(null);
        }

    @PostMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse addTag(@RequestParam(value = "tag")String tag,@RequestParam(value = "house_id") Long houseId){
        if (StringUtils.isEmpty(tag) || null == houseId || houseId<1){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<Boolean> result = houseService.addTag(tag,houseId);
        if (result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
    }

    /**
     * 移除标签接口
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse removeHouseTag(@RequestParam(value = "house_id") Long houseId,
                                      @RequestParam(value = "tag") String tag) {
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult result = this.houseService.removeTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 审核接口
     * @param id
     * @param operation
     * @return
     */
    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable(value = "id")Long id,@PathVariable(value = "operation")int operation){
        if (id<0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult result;
        switch (operation){
            case HouseOperation.PASS:
                result = this.houseService.updateStatus(id, HouseStatus.PASSES.getValue());
                break;
            case HouseOperation.PULL_OUT:
                result = this.houseService.updateStatus(id, HouseStatus.NOT_AUDITED.getValue());
                break;
            case HouseOperation.RENT:
                result = this.houseService.updateStatus(id,HouseStatus.RENTED.getValue());
                break;
            case HouseOperation.DELETE:
                result = this.houseService.updateStatus(id,HouseStatus.DELETED.getValue());
                break;
            default:
                return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        if (result.isSuccess()){
            return ApiResponse.ofSuccess(null);
        }

        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),result.getMessage());
    }



    @GetMapping("admin/house/subscribe")
    public String houseSubscribe() {
        return "admin/subscribe";
    }

    //todo cp
    @GetMapping("admin/house/subscribe/list")
    @ResponseBody
    public ApiResponse subscribeList(@RequestParam(value = "draw") int draw,
                                     @RequestParam(value = "start") int start,
                                     @RequestParam(value = "length") int size) {
        ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> result = houseService.findSubscribeList(start, size);

        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setDraw(draw);
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        return response;
    }

    /**
     * 修改封面接口
     * @param coverId
     * @param targetId
     * @return
     */
    @PostMapping("admin/house/cover")
    @ResponseBody
    public ApiResponse updateCover(@RequestParam(value = "cover_id") Long coverId,
                                   @RequestParam(value = "target_id") Long targetId) {
        ServiceResult result = this.houseService.updateCover(coverId, targetId);

        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

}
