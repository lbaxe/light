//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.light.framework.mvc.filter.shiro;

import javax.servlet.Filter;

public interface ShiroFlagFilter extends Filter {
    default String urlPattern() {
        return "/**";
    }

    default int priority() {
        return 0;
    }
}
