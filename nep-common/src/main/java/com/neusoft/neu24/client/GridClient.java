package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient("grid-service")
public interface GridClient {

    @GetMapping(value = "/grid/select/province/map")
    HttpResponseEntity<Map<Object, Object>> selectProvinceMap();

    @GetMapping(value = "/grid/select/cities/{provinceCode}")
    HttpResponseEntity<Map<Object,Object>> selectCitiesByProvinceCode(@PathVariable("provinceCode") String provinceCode);

    @GetMapping(value = "/grid/select/{townCode}")
    HttpResponseEntity<Grid> selectGridByTownCode(@PathVariable("townCode") String townCode);
}
