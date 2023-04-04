package com.linzhilong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linzhilong.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
