package com.linzhilong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linzhilong.dto.SetmealDto;
import com.linzhilong.entity.Setmeal;
import com.linzhilong.entity.SetmealDish;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    // 添加套餐
    void saveWithDish(SetmealDto setmealDto);

    // 分页查询
    Page getPage(Integer page,Integer pageSize, String name);

    // 单条数据
    SetmealDto getWithDish(Long id);

    // 修改数据
    void updateWithDish(SetmealDto setmealDto);

    // 删除数据
    void deletes(List<Long> ids);

    // 查询套餐列表数据
    List<Setmeal> getList(Setmeal setmeal);

}
