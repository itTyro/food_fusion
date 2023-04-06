package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.common.BaseContext;
import com.linzhilong.entity.ShoppingCart;
import com.linzhilong.mapper.ShoppingCartMapper;
import com.linzhilong.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Override
    public void add(ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        Integer shoppingCartNumber = shoppingCart.getNumber();

        //如果表中已经有相应的数据则把数量加一就行
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart result = this.getOne(queryWrapper);

        if (result != null) {
            // 有该数据，执行修改操作，数量加1
            Integer number = 0;
            if (shoppingCartNumber == null) {
                number = result.getNumber() + 1;
            } else {
                // 给再来一单添加数据准备的
                number = result.getNumber() + shoppingCartNumber;
            }
            result.setNumber(number);
            this.updateById(result);
        } else {
            // 没有该数据，执行添加操作
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
        }
    }
}
