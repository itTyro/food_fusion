package com.linzhilong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linzhilong.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
