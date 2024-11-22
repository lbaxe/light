package com.light.system.domain;

import java.util.StringJoiner;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.light.mapper.entity.BaseEntity;

import lombok.Data;

@Data
@TableName(value = "sys_menu")
public class SysMenu extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @TableId(type = IdType.AUTO)
    private Long menuId;

    /** 菜单名称 */
    private String menuName;

    /** 父菜单名称 */
    @TableField(exist = false)
    private String parentName;

    /** 父菜单ID */
    private Long parentId;

    /** 显示顺序 */
    private String sort;

    /** 菜单URL */
    private String url;

    /** 打开方式（menuItem页签 menuBlank新窗口） */
    private String target;

    /** 类型（M目录 C菜单 F按钮） */
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    private String visible;

    /** 权限字符串 */
    private String perms;

    /** 菜单图标 */
    private String icon;

    @Override
    public String toString() {
        return new StringJoiner(", ", SysMenu.class.getSimpleName() + "[", "]").add("menuId=" + menuId)
            .add("menuName='" + menuName + "'").add("parentName='" + parentName + "'").add("parentId=" + parentId)
            .add("sort='" + sort + "'").add("url='" + url + "'").add("target='" + target + "'")
            .add("menuType='" + menuType + "'").add("visible='" + visible + "'").add("perms='" + perms + "'")
            .add("icon='" + icon + "'").toString();
    }
}
