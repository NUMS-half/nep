package com.neusoft.neu24.grid.controller;

import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.grid.service.IGridService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GridController.class);

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
        } catch ( UpdateException e ) {
            logger.error("更新网格区/县信息时发生异常", e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    @GetMapping(value = "/select/province/map")
    public HttpResponseEntity<Map<Object, Object>> selectProvinceMap() {
        try {
            return gridService.selectProvinceMap();
        } catch ( QueryException e ) {
            logger.error("查询省份信息时发生异常", e);
            return new HttpResponseEntity<Map<Object, Object>>().serverError(null);
        }
    }

    @GetMapping(value = "/select/cities/{provinceCode}")
    public HttpResponseEntity<Map<Object,Object>> selectCitiesByProvinceCode(@PathVariable("provinceCode") String provinceCode) {
        try {
            return gridService.selectCitiesMapByProvince(provinceCode);
        } catch ( QueryException e ) {
            logger.error("查询城市信息发生异常", e);
            return new HttpResponseEntity<Map<Object, Object>>().serverError(null);
        }
    }

    @GetMapping(value = "/select/{townCode}")
    public HttpResponseEntity<Grid> selectGridByTownCode(@PathVariable("townCode") String townCode) {
        try {
            return gridService.selectGridByTownCode(townCode);
        } catch ( QueryException e ) {
            logger.error("根据 townCode 查询网格信息时发生异常", e);
            return new HttpResponseEntity<Grid>().serverError(null);
        }
    }

    @PostMapping(value = "/select/batch")
    public HttpResponseEntity<List<Grid>> selectGridByMultipleTownCodes(@RequestBody List<String> townCodes) {
        try {
            return gridService.selectGridByMultipleTownCodes(townCodes);
        } catch ( QueryException e ) {
            logger.error("根据 townCodes 批量查询网格信息时发生异常", e);
            return new HttpResponseEntity<List<Grid>>().serverError(null);
        }
    }

    @GetMapping(value = "/select/sum")
    public HttpResponseEntity<Map<Object,Object>> selectGridTotal() {
        try {
            return gridService.selectGridTotal();
        } catch ( QueryException e ) {
            logger.error("查询网格总数时发生异常", e);
            return new HttpResponseEntity<Map<Object, Object>>().serverError(null);
        }
    }

}
