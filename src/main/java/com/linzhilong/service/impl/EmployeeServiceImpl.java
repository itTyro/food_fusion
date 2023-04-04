package com.linzhilong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linzhilong.entity.Employee;
import com.linzhilong.mapper.EmployeeMapper;
import com.linzhilong.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
