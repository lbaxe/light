package com.light.framework.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.light.framework.mvc.util.ServletUtil;

@Controller
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @RequestMapping("/authorization/{registrationId}")
    @ResponseBody
    public void authorization(@PathVariable("registrationId") String registrationId) {

    }

    @RequestMapping("/code/{registrationId}")
    @ResponseBody
    public void authorizationCallback(@PathVariable("registrationId") String registrationId, String code) {
        System.out.println(ServletUtil.getRequest().getParameter("code"));
    }
}
