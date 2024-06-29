package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient("grid-service")
public interface GridClient {

    @GetMapping(value = "/grid/select/province/map")
    HttpResponseEntity<Map<Object, Object>> selectProvinceMap();

    @GetMapping(value = "/grid/select/cities")
    HttpResponseEntity<Map<Object,Object>> selectCitiesByProvinceCode(@RequestParam("provinceCode") String provinceCode);

    @GetMapping(value = "/grid/select")
    HttpResponseEntity<Grid> selectGridByTownCode(@RequestParam("townCode") String townCode);

    @PostMapping(value = "/grid/select/batch")
    HttpResponseEntity<List<Grid>> selectGridByMultipleTownCodes(@RequestBody List<String> townCodes);

    @GetMapping(value = "/grid/select/sum")
    HttpResponseEntity<Map<Object,Object>> selectGridTotal();
}
