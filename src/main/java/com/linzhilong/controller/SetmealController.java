package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linzhilong.common.R;
import com.linzhilong.dto.SetmealDto;
import com.linzhilong.entity.Setmeal;
import com.linzhilong.entity.SetmealDish;
import com.linzhilong.service.SetmealDishService;
import com.linzhilong.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("接收到的数据：{}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("套餐已添加成功");
    }

    /**
     * 分页查询，需要将分类的名称也查出来
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> getPage(Integer page,Integer pageSize, String name) {
        log.info("套餐管理查询的page：{}，pageSize：{}，name：{}",page,pageSize,name);

        Page dtoPage = setmealService.getPage(page, pageSize, name);
        return R.success(dtoPage);
    }

    /**
     * 根据id查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info("套餐管理需要修改的id：{}",id);

        if (id != null) {
            SetmealDto setmealDto = setmealService.getWithDish(id);
            return R.success(setmealDto);
        }
        return R.error("参数有误");
    }

    /**
     * 修改数据
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("套餐管理修改数据接受的参数：{}",setmealDto.toString());

        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐信息成功");
    }

    /**
     * 修改状态
     * @param status 拼在路径上用@PathVariable获取
     * @param ids 数组对象用@RequestParam("ids")
     * @return 返回提示信息
     */
    @PostMapping("/status/{status}")
    public R<String> putStatus(@PathVariable Integer status, @RequestParam("ids") List<Long> ids) {
        log.info("status：{},ids: {}",status,ids.toString());

        if (ids.isEmpty()) {
            R.error("参数有误");
        }

        // 创建一个Setmeal，设置要修改的状态和id，根据id修改状态
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        for (Long id : ids) {
            setmeal.setId(id);
            setmealService.updateById(setmeal);
        }

        return R.success("状态修改成功");
    }

    /**
     * 删除套餐，售卖状态时不可删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("需要删除的数据：{}", ids.toString());
        if (ids.isEmpty() ) {
            return R.error("参数有误");
        }

        setmealService.deletes(ids);
        return R.success("删除套餐成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("查询套餐全部数据。。。");
        List<Setmeal> list = setmealService.getList(setmeal);
        return R.success(list);
    }
}
