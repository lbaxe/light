package com.light.web.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.light.framework.mvc.response.JsonResult;

@RequestMapping("/api")
@RestController
public class TestController {
    @RequestMapping("/test")
    public JsonResult test() {
        return JsonResult.success();
    }

    public void test1() {
        System.out.println("test1====");
    }
}
