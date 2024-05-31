package com.neusoft.neu24.grid.service;


import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IGridService {

    /**
     * 获取网格层次信息
     * @return 网格层次树
     */
    HttpResponseEntity<Map<String, Object>> getGridTree();

    /**
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    HttpResponseEntity<Boolean> updateGridTown(Grid grid);
}

