package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.entity.User;
import com.linzhilong.mapper.UserMapper;
import com.linzhilong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
