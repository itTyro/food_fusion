package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linzhilong.common.R;
import com.linzhilong.entity.Employee;
import com.linzhilong.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        // 1. 对获取到的密码进行md5加密,然后才能比对数据库的数据
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());

        // 2. 根据用户名查询
        String username = employee.getUsername();
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, username);
        Employee result = employeeService.getOne(lqw);

        // 3. 如果查得到说明用户名正确,否则直接提示用户
        if (result == null) {
            return R.error("用户名错误,登陆失败");
        }

        // 4. 进行密码的验证
        if (!result.getPassword().equals(password)) {
            return R.error("用户名或密码错误");
        }

        // 5. 判断当前账号的状态,0 表示禁用, 1 表示可用
        if (result.getStatus() == 0) {
            return R.error("当前账号已被冻结,请联系管理员");
        }

        // 6. 登录成功,将员工id存入Session并返回登陆结果
        request.getSession().setAttribute("employee", result.getId());

        return R.success(result);
    }


    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理Session中保存的当前登录员工的数据
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("要添加的信息：{}",employee.toString());

        // 设置初始密码, 需要md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置创建时间
        //employee.setCreateTime(LocalDateTime.now());
        // 设置最后修改时间
        //employee.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户的id
        long empId = (long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("添加员工成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> getPage(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();

        // 添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        // 添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        log.info("------------查询------------");
        employeeService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    /**
     * 修改状态
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("传过来的数据 => {}",employee.toString());

        //更新修改的时间
//        employee.setUpdateTime(LocalDateTime.now());

        // 获取修改人id
//        long empId = (long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
        log.info("-----------修改-----------");
        employeeService.updateById(employee);

        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id) {
        log.info("传过来的id = {}",id);
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("查询不到数据");
    }
}
