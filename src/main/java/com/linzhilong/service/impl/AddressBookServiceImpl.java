package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.entity.AddressBook;
import com.linzhilong.mapper.AddressBookMapper;
import com.linzhilong.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
