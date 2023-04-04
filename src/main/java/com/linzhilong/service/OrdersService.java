package com.linzhilong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linzhilong.entity.Orders;


public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);

    Page userPage(Integer page,Integer pageSize);

    // 后台订单页面
    Page getPage(Integer page, Integer pageSize, String number, String beginTime,String endTime);

    // 再来一单
    void again(Long id);
}
