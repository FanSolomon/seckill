package com.ytz.seckill.controller;

import com.ytz.seckill.redis.RedisService;
import com.ytz.seckill.result.CodeMsg;
import com.ytz.seckill.result.Result;
import com.ytz.seckill.service.MiaoshaUserService;
import com.ytz.seckill.vo.LoginVo;
import com.ytz.seckill.vo.RegisterVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/to_register")
    public String toRegister() {
        return "register";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //登录
        String token = userService.login(response, loginVo);
        return Result.success(token);
    }

    @RequestMapping("/do_register")
    @ResponseBody
    public Result<String> doRegister(HttpServletResponse response, @Valid RegisterVo registerVo) {
        log.info(registerVo.toString());
        //注册
        Boolean flag = userService.register(registerVo);
        if (flag) {
            return Result.success("success");
        }
        return Result.error(CodeMsg.INSERT_USER_ERROR);
    }
}
