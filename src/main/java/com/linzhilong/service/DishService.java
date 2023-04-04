package com.linzhilong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linzhilong.dto.DishDto;
import com.linzhilong.entity.Dish;

public interface DishService extends IService<Dish> {
    /**
     * 添加数据
     * @param dishDto
     */
    void saveWithFlavor(DishDto dishDto);

    /**
     * 分页查询
     * @param id
     * @return
     */
    DishDto getWithFlavor(Long id);

    /**
     * 修改数据
     * @param dishDto
     */
    void updateWithFlavor(DishDto dishDto);

    boolean deleteImg(String fileName);

}
