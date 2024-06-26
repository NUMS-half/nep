package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("aqi-service")
public interface AqiClient {

    @GetMapping("/aqi/select/all")
    HttpResponseEntity<List<Aqi>> selectAllAqi();

    @PostMapping("/aqi/validate")
    HttpResponseEntity<Boolean> validateAqi(@RequestBody Statistics statistics);
}
