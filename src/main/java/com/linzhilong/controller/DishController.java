package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linzhilong.common.CustomException;
import com.linzhilong.common.R;
import com.linzhilong.dto.DishDto;
import com.linzhilong.entity.Category;
import com.linzhilong.entity.Dish;
import com.linzhilong.entity.DishFlavor;
import com.linzhilong.entity.SetmealDish;
import com.linzhilong.service.CategoryService;
import com.linzhilong.service.DishFlavorService;
import com.linzhilong.service.DishService;
import com.linzhilong.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("接收到的数据：{}", dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);

        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 查询页面数据
        dishService.page(pageInfo, queryWrapper);

        // 将dish数据拷贝到dishDto里，但是排除展示的数据，需要进行处理
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        // 将pageInfo里的records拿出来进行遍历，可以得到每一条数据的id，再根据id查询可以获得菜品分类的名称
        List<Dish> list = pageInfo.getRecords();
        // 创建一个空的集合来存储DishDto对象的数据，后续可以将集合set到dishDtoPage的records中
        List<DishDto> dishDtoList = new ArrayList<>();

        // 遍历集合里的数据，也可以用stream流进行遍历
        for (Dish dish : list) {
            // 获取菜品对应的分类id
            Long categoryId = dish.getCategoryId();
            // 根据id查询到对应分类
            Category category = categoryService.getById(categoryId);
            // 创建Dto对象，存储返回的数据
            DishDto dishDto = new DishDto();
            if (category != null) {
                // 获取分类名
                String categoryName = category.getName();
                // 将名字设置进去
                dishDto.setCategoryName(categoryName);

            }
            // 其他数据拷贝
            BeanUtils.copyProperties(dish, dishDto);
            //添加到集合
            dishDtoList.add(dishDto);
        }
        // 将集合添加到records
        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询单个
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        log.info("查询的id = {}", id);

        if (id == null) {
            return R.error("查询失败");
        }

        DishDto dishDto = dishService.getWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改数据
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("接收到的数据：{}", dishDto);

        dishService.updateWithFlavor(dishDto);

        return R.success("修改成功");
    }

    /**
     * 修改商品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam("ids") long[] ids) {
        log.info("状态修改的数据：status：{},ids：{}", status, Arrays.toString(ids));

        Dish dish = new Dish();
        dish.setStatus(status);
        for (long id : ids) {
            dish.setId(id);
            dishService.updateById(dish);
        }
        return R.success("修改成功");
    }

    /**
     * 删除菜品，对应的口味也应该删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("要删除的id：{}", ids);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        if (setmealDishService.count(lambdaQueryWrapper) > 0) {
            throw new CustomException("套餐中有此菜品，不允许删除");
        }


        for (Long id : ids) {
            // 获取对应的照片文件名，删除照片   ===>   更好的办法是重写删除方法，在删除方法里写删除照片
            String fileName = dishService.getById(id).getImage();
            boolean flag = dishService.deleteImg(fileName);
            log.info("照片删除结果：{}",flag);
            // 删除完照片之后删除数据
            dishService.removeById(id);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(queryWrapper);
        }
        return R.success("删除成功");
    }

    /**
     * 查询列表数据
     * @param dish 用对象接收通用性更强
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list( Dish dish) {
        log.info("查询list的数据是：{}",dish.toString());
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 根据id查询
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        // 根据name模糊查询
        queryWrapper.like(dish.getName() != null,Dish::getName,dish.getName());
        queryWrapper.eq(Dish::getStatus,1);
        // 排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //查询数据
        List<Dish> dishList = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish1 : dishList) {
            Long id = dish1.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,id);
            List<DishFlavor> flavorList = dishFlavorService.list(queryWrapper1);

            DishDto dishDto = new DishDto();
            dishDto.setFlavors(flavorList);
            BeanUtils.copyProperties(dish1,dishDto);
            dishDtoList.add(dishDto);
        }
        return R.success(dishDtoList);
    }
}
