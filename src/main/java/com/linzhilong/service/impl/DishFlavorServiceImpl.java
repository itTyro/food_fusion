package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.entity.DishFlavor;
import com.linzhilong.mapper.DishFlavorMapper;
import com.linzhilong.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
