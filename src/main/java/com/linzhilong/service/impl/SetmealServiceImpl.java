package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.common.CustomException;
import com.linzhilong.dto.SetmealDto;
import com.linzhilong.entity.Setmeal;
import com.linzhilong.entity.SetmealDish;
import com.linzhilong.mapper.SetmealMapper;
import com.linzhilong.service.CategoryService;
import com.linzhilong.service.DishService;
import com.linzhilong.service.SetmealDishService;
import com.linzhilong.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 添加操作
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 添加到套餐表
        this.save(setmealDto);
        // 取出套餐中的id
        Long setmealId = setmealDto.getId();

        // 取出套餐中的菜品
        List<SetmealDish> dishList = setmealDto.getSetmealDishes();
        for (SetmealDish dish : dishList) {
            // 设置表中字段
            dish.setSetmealId(setmealId);

            // 添加操作
            setmealDishService.save(dish);
        }

    }

    /**
     * 查询页面
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page getPage(Integer page, Integer pageSize, String name) {

        // 不止setmeal表里的数据，还需要查询套餐的分类，所以需要在查询分类名
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        // SetmealDto表里有对应的字段
        Page<SetmealDto> setmealDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        // 排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 查询套餐页面
        this.page(pageInfo,queryWrapper);

        // 将pageInfo页面数据拷到setmealPage
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        // 获取查询到的数据
        List<Setmeal> setmealList = pageInfo.getRecords();
        // 创建一个集合存储需要的数据
        List<SetmealDto> setmealDtoList = new ArrayList<>();

        for (Setmeal setmeal : setmealList) {
            SetmealDto setmealDto = new SetmealDto();
            Long categoryId = setmeal.getCategoryId();
            // 获取对应套餐分类名
            String categoryName = categoryService.getById(categoryId).getName();
            setmealDto.setCategoryName(categoryName);
            // 其他数据拷贝
            BeanUtils.copyProperties(setmeal,setmealDto);
            // 添加到集合
            setmealDtoList.add(setmealDto);
        }

        // 设置setmealDtoPage里的records
        setmealDtoPage.setRecords(setmealDtoList);

        return setmealDtoPage;
    }

    /**
     * 根据id查询数据返回
     * 需要用到两个表的数据，都封装到setmealDto里，用对象拷贝
     * @param id
     * @return
     */
    @Override
    public SetmealDto getWithDish(Long id) {

        SetmealDto setmealDto = new SetmealDto();
        // 1、先获取setmeal表的数据
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);

        // 2、条件构造器来查询菜品表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        // 3、添加到setmealDto
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }


    /**
     * 修改数据
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        // 获取id
        Long setmealId = setmealDto.getId();
        // 判断图片信息是否发生改变，改变则删除原有图片
        String fillName = this.getById(setmealId).getImage();
        if (!fillName.equals(setmealDto.getImage())) {
            boolean flag = dishService.deleteImg(fillName);
            log.info("照片删除的结果：{}",flag);
        }

        // 修改setmeal表中的数据
        this.updateById(setmealDto);


        // 条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);

        // 获取菜品数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        // 先清空有关数据，再添加数据
        setmealDishService.remove(queryWrapper);
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时需要删除套餐下的菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deletes(List<Long> ids) {

        // 正在售卖的套餐不可以删除
        LambdaQueryWrapper<Setmeal> statusQueryWrapper = new LambdaQueryWrapper<>();
        statusQueryWrapper.in(Setmeal::getId,ids);
        statusQueryWrapper.eq(Setmeal::getStatus,1);
        if (this.count(statusQueryWrapper) > 0) {
            throw new CustomException("请先停售套餐");
        }


        for (Long id : ids) {
            // 删除套餐的图片
            String fileName = this.getById(id).getImage();
            boolean flag = dishService.deleteImg(fileName);
            log.info("图片删除的结果是：{}",flag);

            // 条件构造器，删除套餐下的菜品
            LambdaQueryWrapper<SetmealDish> queryWrapper  = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,id);
            setmealDishService.remove(queryWrapper);
        }
        this.removeByIds(ids);
    }

    @Override
    public List<Setmeal> getList(Setmeal setmeal) {

        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 根据id和status进行查询
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return this.list(queryWrapper);
    }


}
