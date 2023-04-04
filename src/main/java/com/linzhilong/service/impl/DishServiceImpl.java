package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.dto.DishDto;
import com.linzhilong.entity.Dish;
import com.linzhilong.entity.DishFlavor;
import com.linzhilong.mapper.DishMapper;
import com.linzhilong.service.DishFlavorService;
import com.linzhilong.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Value("${reggie.path}")
    private String imgPath;


    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味
     *
     * @param dishDto
     */
    @Override
    @Transactional  // 事务控制，保证事务的一致性
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品到dish表中
        this.save(dishDto);

        // 获取菜品的id
        Long dishId = dishDto.getId();
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // flavors里id没有赋值，进行赋值
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
            log.info("id = {}", flavor.getId());
        }

        // 保存菜品口味数据到菜品口味表中
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查单个数据，需要将两个表的数据一起查出来，可以用dto映射
     * @param id
     * @return
     */
    @Override
    public DishDto getWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(id);

        // 条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        // 查询结果
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        // 设置dishDto
        dishDto.setFlavors(list);
        BeanUtils.copyProperties(dish,dishDto);
        return dishDto;
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {

        // 获取当前菜品的id
        Long dishId = dishDto.getId();
        // 查询原有的图片名，进行对比, 需要在修改数据之前获取原始文件名，否则就是修改后的数据
        String fileName = this.getById(dishId).getImage();

        // 修改dish表
        this.updateById(dishDto);

        // 获取传过来数据菜品的图片名，判断是否进行了修改，进行修改的话则删除原来的图片
        String dishImg = dishDto.getImage();

        if (!fileName.equals(dishImg)) {
            boolean flag = this.deleteImg(fileName);
            log.info("照片删除结果：{}",flag);
        }


        // 获取需要修改的口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        // 条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);

        // 无论表中是否有数据，直接先清空，前端会将原有的数据带过来再进行添加
        dishFlavorService.remove(queryWrapper);
        if (!flavors.isEmpty()) {
            // 需要设置flavor里的dishId，否则报错
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorService.saveBatch(flavors);
        }


    }

    /**
     * 删除本地照片
     * @param fileName
     */
    @Override
    public boolean deleteImg(String fileName) {
        // 创建文件路径
        File file = new File(imgPath + fileName);

        if (file.exists() && file.isFile()) {
            boolean flag = file.delete();
            return flag;
        }
        return false;
    }
}
