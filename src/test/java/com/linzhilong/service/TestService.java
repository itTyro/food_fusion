package com.linzhilong.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestService {
    @Autowired
    private CategoryService categoryService;

    @Test
    public void testRemove() {
        categoryService.remove(1640179186730024961L);
    }
}
