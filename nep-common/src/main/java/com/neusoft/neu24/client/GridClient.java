package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("grid-service")
public interface GridClient {

    @GetMapping(value = "/grid/select/{townCode}")
    HttpResponseEntity<Grid> selectGridByTownCode(@PathVariable("townCode") String townCode);
}
