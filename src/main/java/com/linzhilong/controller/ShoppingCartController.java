package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linzhilong.common.BaseContext;
import com.linzhilong.common.R;
import com.linzhilong.entity.ShoppingCart;
import com.linzhilong.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查询购物车信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查询购物车信息。。。");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<String> save(@RequestBody ShoppingCart shoppingCart) {
        log.info("要添加的数据：{}", shoppingCart.toString());

        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //如果表中已经有相应的数据则把数量加一就行
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart result = shoppingCartService.getOne(queryWrapper);

        if (result != null) {
            // 有该数据，执行修改操作，数量加1
            Integer number = result.getNumber() + 1;
            result.setNumber(number);
            shoppingCartService.updateById(result);
        } else {
            // 没有该数据，执行添加操作
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success("成功添加购物车");
    }

    /**
     * 减少数量
     * 如果数量大于1则修改数据-1
     * 如果数量等于1则删除数据
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> deleteById(@RequestBody ShoppingCart shoppingCart) {
        log.info("减少数量。。。");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 查询数据进行判断
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        // 查询数据
        ShoppingCart result = shoppingCartService.getOne(queryWrapper);
        Integer number = result.getNumber();
        if (number > 1) {
            // 数量大于1，减少数量
            result.setNumber(number - 1);
            shoppingCartService.updateById(result);
        } else {
            // 数量等于1，直接删除数据
            shoppingCartService.remove(queryWrapper);
        }

        return R.success("操作成功");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        log.info("清空购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("已全部清空");

    }

}
