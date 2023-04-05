package com.linzhilong.dto;


import com.linzhilong.entity.Setmeal;
import com.linzhilong.entity.SetmealDish;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SetmealDto extends Setmeal implements Serializable {

    // 添加的菜品集合
    private List<SetmealDish> setmealDishes;

    // 套餐分类名
    private String categoryName;
}
