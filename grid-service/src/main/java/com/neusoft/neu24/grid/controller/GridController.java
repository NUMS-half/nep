package com.neusoft.neu24.grid.controller;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.grid.service.IGridService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 网格前端控制器
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@RestController
@RequestMapping("/grid")
public class GridController {

    @Resource
    private IGridService gridService;

    /**
     * 更新网格区/县信息
     * @param map 网格区/县信息map
     * @return 是否更新成功
     */
    @PostMapping(value = "/updateGridTown", headers = "Content-Type=application/json")
    public HttpResponseEntity<Boolean> updateGridTown(@RequestBody Map<String, Object> map) {
        Grid grid = new Grid();
        try {
            grid.setTownId((Integer) map.get("townId"));
            grid.setTownName((String) map.get("townName"));
            grid.setTownCode((String) map.get("townCode"));
            grid.setCityCode((String) map.get("cityCode"));
            return gridService.updateGridTown(grid);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    @GetMapping(value = "/select/province/map")
    public HttpResponseEntity<Map<Object, Object>> selectProvinceMap() {
        return gridService.selectProvinceMap();
    }

    @GetMapping(value = "/select/cities/{provinceCode}")
    public HttpResponseEntity<Map<Object,Object>> selectCitiesByProvinceCode(@PathVariable("provinceCode") String provinceCode) {
        return gridService.selectCitiesMapByProvince(provinceCode);
    }

    @GetMapping(value = "/select/{townCode}")
    public HttpResponseEntity<Grid> selectGridByTownCode(@PathVariable("townCode") String townCode) {
        return gridService.selectGridByTownCode(townCode);
    }



}
