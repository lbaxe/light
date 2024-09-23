package com.light.system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.light.framework.mvc.controller.BaseController;
import com.light.system.domain.SysMenu;
import com.light.system.mapper.SysMenuMapper;

@Controller
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private SysMenuMapper sysMenuMapper;

    @PostMapping("/list")
    @ResponseBody
    public List<SysMenu> list() {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(new QueryWrapper());
        return sysMenus;
    }

    // thymeleaf
    @PostMapping("/list1")
    public String list1(Model model) {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(new QueryWrapper());
        model.addAttribute("list", sysMenus);
        return "list1";
    }

    // freemarker
    @PostMapping("/list2")
    public String list2(Model model) {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(new QueryWrapper());
        model.addAttribute("list", sysMenus);
        return "list2";
    }

    // jsp
    @PostMapping("/list3")
    public String list3(Model model) {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(new QueryWrapper());
        model.addAttribute("list", sysMenus);
        return "list3";
    }
}