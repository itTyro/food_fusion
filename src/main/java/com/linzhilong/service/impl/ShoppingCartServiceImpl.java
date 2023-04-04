package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.entity.ShoppingCart;
import com.linzhilong.mapper.ShoppingCartMapper;
import com.linzhilong.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
