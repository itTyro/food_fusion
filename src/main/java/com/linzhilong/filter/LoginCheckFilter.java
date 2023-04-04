package com.linzhilong.filter;

import com.alibaba.fastjson.JSON;
import com.linzhilong.common.BaseContext;
import com.linzhilong.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginFilter",description = "/*")
public class LoginCheckFilter implements Filter {

    //spring进行路径判断的对象
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 1.强转成HTTP Servlet
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 2. 获取请求的路径
        String uri = request.getRequestURI();
        log.info("拦截的路径为: ==>{}",uri);

        // 3. 定义不需要处理的路径目录
        String[] uris = new String[] {
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/user/sendMsg",
          "/user/login"
        };

        // 4. 判断拦截的路径需不需要处理
        boolean check = check(uris, uri);
        if (check) {
            //不需要处理,放行,结束方法
            log.info("路径不需要处理");
            filterChain.doFilter(request,response);
            return;
        }

        // 5-1(员工端). 判断用户是否登录
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录");

            // 往当前线程容器存储id
            long empId = (long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        // 5-2(用户端). 判断用户是否登录
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录");

            // 往当前线程容器存储id
            long userId = (long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        // 6. 未登录 进行拦截相应跳转
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private boolean check(String[] uris, String uri) {

        for (String url : uris) {
            if(PATH_MATCHER.match(url,uri)) {
                return true;
            }
        }
        return false;
    }
}
