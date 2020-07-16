package com.imooc.web.controller.house;

import com.imooc.base.ApiResponse;

import com.imooc.base.RentValueBlock;
import com.imooc.entity.SupportAddress;
import com.imooc.service.IUserService;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.service.house.AddressService;
import com.imooc.service.house.HouseService;
import com.imooc.web.dto.*;
import com.imooc.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class HouseController {
    @Autowired
    AddressService addressService;

    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result = addressService.getSupportCities();
        if (result.getResultSize() == 0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam("city_name") String cityName){
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllRegionsByCityName(cityName);
        if (result.getResultSize() == 0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取具体城市支持的地铁线路
     * @param cityName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam("city_name") String cityName){
        ServiceMultiResult<SubwayDTO> result = addressService.findAllSubwayByCity(cityName);
        if (result.getResultSize() == 0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取具体城市支持的地铁线路
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam("subway_id") Long subwayId){
        ServiceMultiResult<SubwayStationDTO> result = addressService.findAllStationBySubway(subwayId);
        if (result.getResultSize() == 0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    @Autowired
    HouseService houseService;

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch, Model model, HttpSession session,
                                RedirectAttributes redirectAttributes){

        if (rentSearch.getCityEnName() == null){
            String cityEnNameInSession = (String)session.getAttribute("cityEnName");
            if (cityEnNameInSession == null){
                redirectAttributes.addAttribute("msg","must_chose_city");
                return "redirect:/index";
            }else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        }else {
            session.setAttribute("cityEnName",rentSearch.getCityEnName());
        }

        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()){
            redirectAttributes.addAttribute("msg","must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity",city.getResult());

        ServiceMultiResult<SupportAddressDTO>  addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal()<1){
            redirectAttributes.addAttribute("msg","must_chose_city");
            return "redirect:/index";
        }

       ServiceMultiResult<HouseDTO> serviceMultiResult =  houseService.query(rentSearch);
        model.addAttribute("total",serviceMultiResult.getTotal());
        model.addAttribute("houses",serviceMultiResult.getResult());

        if (rentSearch.getRegionEnName() == null){
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody",rentSearch);
        model.addAttribute("regions",addressResult.getResult());
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks",RentValueBlock.AREA_BLOCK);
        model.addAttribute("currentPriceBlock",RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock",RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

     @Autowired
    IUserService userService;

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id")Long houseId,Model model){
        if (houseId<=0){
            return "404";
        }
        ServiceResult<HouseDTO> serviceResult = houseService.getHouseInfoById(houseId);
        if (!serviceResult.isSuccess()){
            return "404";
        }

        HouseDTO houseDTO = serviceResult.getResult();
        Map<SupportAddress.Level,SupportAddressDTO> addressDTOMap = addressService.findCityAndRegion(houseDTO.getCityEnName(),houseDTO.getRegionEnName());
        SupportAddressDTO city = addressDTOMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = addressDTOMap.get(SupportAddress.Level.REGION);
        model.addAttribute("city",city);
        model.addAttribute("region",region);

        ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());
        model.addAttribute("agent",userDTOServiceResult.getResult());
        model.addAttribute("house",houseDTO);

        model.addAttribute("houseCountInDistrict",0);
        return "house-detail";

    }











}
