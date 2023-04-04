package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.common.BaseContext;
import com.linzhilong.common.CustomException;
import com.linzhilong.dto.OrdersDto;
import com.linzhilong.entity.*;
import com.linzhilong.mapper.OrdersMapper;
import com.linzhilong.service.*;
import com.linzhilong.utils.OrderIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 添加订单信息
     * 需要查询购物车信息，然后添加到订单信息表和订单信息明细表
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();

        // 查询当前用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new CustomException("购物车为空，不能下单");
        }

        // 查询用户信息
        User user = userService.getById(userId);

        // 查询地址簿信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        // 生成订单id
//            Long orderId = OrderIdUtils.getOrderId();
        long orderId = IdWorker.getId();

        // 获取实收金额
//        int amount = 0;
        AtomicInteger amount = new AtomicInteger(0);  //保证线程安全

        //订单明细表集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
//            amount += shoppingCart.getNumber() * shoppingCart.getAmount().intValue();
            // 计算实收金额
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            // 需要将信息明细拷贝到订单信息明细表里
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            log.info("生成的id：{}", orderId);
            orderDetail.setOrderId(orderId);
            log.info("订单明细表：{}", orderDetail.toString());
            orderDetailList.add(orderDetail);
        }
        log.info("实收金额：{}", amount);

        // 设置订单表信息
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 向表中插入数据
        this.save(orders);

        // 向明细表中插入数据
        orderDetailService.saveBatch(orderDetailList);

        // 清空购物车
        shoppingCartService.remove(queryWrapper);

    }

    /**
     * 用户查询订单页面
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page userPage(Integer page, Integer pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);

        this.page(pageInfo, queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        // 获取原数据
        List<Orders> ordersList = pageInfo.getRecords();

        // 最终数据的集合
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders orders : ordersList) {

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders, ordersDto);

            Long orderId = orders.getId();
            //查询订单菜品
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(orderId != null, OrderDetail::getOrderId, orderId);
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);
            // 添加到dto的菜品集合
            ordersDto.setOrderDetails(list);
            ordersDtoList.add(ordersDto);

        }

        // 添加到要返回的records
        ordersDtoPage.setRecords(ordersDtoList);

        return ordersDtoPage;
    }

    /**
     * 后台查询订单
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public Page getPage(Integer page, Integer pageSize, String number, String beginTime, String endTime) {


        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (beginTime != null) {

            queryWrapper.ge(Orders::getOrderTime, LocalDateTime.parse(beginTime, formatter));
        }
        if (endTime != null) {

            queryWrapper.le(Orders::getOrderTime, LocalDateTime.parse(endTime, formatter));
        }

        this.page(pageInfo, queryWrapper);

        return pageInfo;
    }

    @Override
    @Transactional
    public void again(Long id) {
        // 根据id查询订单明细表中的数据
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,OrderDetail::getOrderId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);

        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : list) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartService.saveBatch(shoppingCartList);

        // 将数据重新添加到购物车

    }


}
