package com.linzhilong.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDishService {
    @Autowired
    private DishService dishService;

    @Test
    public void testDeleteImg() {
        String path = "E:\\Project\\reggieImg\\";
        dishService.deleteImg("f89e884d-7318-40f3-b028-c92eac779423.jpg");

    }
}
