package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.linzhilong.common.BaseContext;
import com.linzhilong.common.R;
import com.linzhilong.entity.AddressBook;
import com.linzhilong.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        log.info("接收到的地址信息：{}",addressBook.toString());

        // 获取用户id
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("地址添加成功");
    }


    /**
     * 查询地址列表信息
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        log.info("查询列表数据");
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,userId);
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 根据id查询地址信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        log.info("要查询的地址id：{}",id);
        if (id !=null) {
            AddressBook addressBook = addressBookService.getById(id);
            return R.success(addressBook);
        }
        return R.error("无信息");
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        log.info("修改的数据：{}",addressBook.toString());
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 修改默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        log.info("修改默认地址的id：{}",addressBook.getId());
        Long userId = BaseContext.getCurrentId();

        //默认地址只能有一个，所以需要将之前的默认地址改掉
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId,userId);
        wrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        log.info("查询用户默认地址");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook == null) {
            return R.error("没有默认地址");
        }
        return R.success(addressBook);
    }

    /**
     * 删除地址信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        log.info("需要删除的id是：{}",ids);
        addressBookService.removeByIds(ids);

        return R.success("删除地址成功");
    }
}
