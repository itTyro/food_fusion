package com.linzhilong.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.linzhilong.entity.Orders;
import com.linzhilong.utils.OrderIdUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
public class TestOrdersService {

    @Autowired
    private OrdersService ordersService;

    @Test
    void testAmount() {
        // 由于方法中的用户id是动态获取，测试获取不到，所以先将用户id写死
        Orders orders = new Orders();
        orders.setAddressBookId(1641730901698699265L);
        ordersService.submit(orders);
    }

    @Test
    void testLocalData() throws InterruptedException {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
//        for (int i = 0; i < 100; i++) {
//            LocalDateTime now = LocalDateTime.now();
////            Thread.sleep(100);
//            System.out.println(now.format(formatter));

//    }
//        System.out.println(OrderIdUtils.getOrderId());
        System.out.println(IdWorker.getId());
    }

    @Test
    void testNanoTime()  {
        for (int i = 0; i < 100; i++) {
            long nanoTime = System.nanoTime();
            System.out.println(Long.toString(nanoTime));
        }
    }
}
