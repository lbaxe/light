package com.light.mapper.injector;

import java.util.ArrayList;
import java.util.List;

public class ColumnConst {
    public static final List<String> STANDARD_COLUMNS = new ArrayList<>();
    static {
        STANDARD_COLUMNS.add("delete_flag");
        STANDARD_COLUMNS.add("create_time");
        STANDARD_COLUMNS.add("update_time");
    }
}
