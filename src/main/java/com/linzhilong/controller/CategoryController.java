package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linzhilong.common.R;
import com.linzhilong.entity.Category;
import com.linzhilong.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category => {}",category.toString());
        categoryService.save(category);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page( int page, int pageSize) {

        log.info("page = {},pageSize = {}",page,pageSize);
        Page<Category> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Category::getSort);

        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {

        log.info("category = {}",category.toString());

        if (category.getId() != null) {

        categoryService.updateById(category);
        return R.success("修改成功");
        }

        return R.error("修改失败");
    }

    /**
     * 根据id删除数据
     * @param ids
     * @return
     */

    @DeleteMapping()
    public R<String> delete( long ids) {

        log.info("要删除的id = {}",ids);

//        categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }


    @GetMapping("/list")
    public R<List<Category>> list(Category category) {  //不是直接获取type，因为封装到对象里通用性更强
        log.info("查询的类型是：{}",category.getType());

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<Category>();
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        // 添加排序，根据sort升序排，如果相同则按照修改时间降序排
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
