package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("statistics-service")
public interface AqiClient {

    @GetMapping("/aqi/select/all")
    HttpResponseEntity<List<Aqi>> selectAllAqi();
}
