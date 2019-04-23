package com.ytz.seckill.controller;

import com.ytz.seckill.result.CodeMsg;
import com.ytz.seckill.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name","ytz");
        return "hello";
    }

    @RequestMapping("/success")
    @ResponseBody
    public Result<String> success() {
        return Result.success("success");
    }

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }
}
