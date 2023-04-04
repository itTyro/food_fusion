package com.linzhilong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linzhilong.common.R;
import com.linzhilong.entity.Orders;
import com.linzhilong.service.OrderDetailService;
import com.linzhilong.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("接收到的数据：{}",orders.toString());
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户端查询订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(Integer page,Integer pageSize) {
        log.info("page:{},pageSize:{}",page,pageSize);
        Page userPage = ordersService.userPage(page, pageSize);
        return R.success(userPage);
    }

    /**
     * 后台查询订单信息
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String number, String beginTime, String endTime) {
        log.info("后台查询订单。。。page:{},pageSize{},beginTime{},endTime{}",page,pageSize,beginTime,endTime);

        Page result = ordersService.getPage(page, pageSize, number, beginTime, endTime);
        return R.success(result);
    }

    /**
     * 更改状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        log.info("修改状态：{}",orders.toString());
        ordersService.updateById(orders);
        return R.success("更新成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        log.info("再来一单的id是：{}",orders.getId());
        ordersService.again(orders.getId());
        return R.success("c");
    }
}
