package com.linzhilong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linzhilong.common.R;
import com.linzhilong.entity.User;
import com.linzhilong.service.UserService;
import com.linzhilong.utils.SMSUtils;
import com.linzhilong.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info("接收到的手机号：{}",user.getPhone());

        // 判断手机号码是否为空
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
        // 生成验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("生成的验证码：{}",code);

        // 发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

        // 存储验证码
            session.setAttribute(phone,code);
            return R.success("验证码发送成功");
        }

        return R.error("网络错误");
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody Map<String,String> map,HttpSession session) {
        log.info("接收到的数据：{}",map.toString());
        // 获取手机号和验证码
        String phone = map.get("phone");
        String code = map.get("code");

        // 判断手机号和验证码是否为空
        if (phone != null && code != null ) {
            // 获取session中的验证码
            String codeInSession = (String) session.getAttribute(phone);

        // 判断验证码是否正确
            if (codeInSession != null && codeInSession.equals(code)) {
                // 验证码正确，查询有无注册过
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone,phone);
                User user = userService.getOne(queryWrapper);

                if (user == null) {
                    // 说明用户从未注册，自动注册
                    user = new User();
                    user.setPhone(phone);
                    user.setStatus(1);
                    userService.save(user);
                }

                // 将登录用户的id存在session里
                session.setAttribute("user",user.getId());
                return R.success("登录成功");

            }
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {
        log.info("退出登录");
        // 清除存储的session
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
