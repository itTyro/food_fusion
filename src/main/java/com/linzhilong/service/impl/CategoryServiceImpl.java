package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.common.CustomException;
import com.linzhilong.entity.Category;
import com.linzhilong.entity.Dish;
import com.linzhilong.entity.Setmeal;
import com.linzhilong.mapper.CategoryMapper;
import com.linzhilong.service.CategoryService;
import com.linzhilong.service.DishService;
import com.linzhilong.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除数据，判断分类下是否有关联菜品
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 菜品分类
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        // 查询菜品分类下关联多少数据
        int count1 = dishService.count(dishLambdaQueryWrapper);

        // 判断有无数据
        if (count1 > 0) {
            // 抛出自定义业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 套餐分类
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if (count2 > 0) {
            // 抛出自定义业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 无关联数据，正常删除
        super.removeById(id);

    }
}
