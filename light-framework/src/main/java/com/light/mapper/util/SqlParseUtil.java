package com.light.mapper.util;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SqlParseUtil {
    private static CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public static List<String> getTableList(String sql) {
        if (sql == null || sql.trim().equals("")) {
            return Collections.emptyList();
        }
        Statement statement = null;
        try {
            statement = parserManager.parse(new StringReader(sql));
        } catch (JSQLParserException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tablesNames = tablesNamesFinder.getTableList(statement);
        return tablesNames == null ? Collections.emptyList() : tablesNames;
    }

    public static void main(String[] args) throws JSQLParserException {
        String sql = "SELECT ax.ADDRESS_CITY, COUNT(1)\n" + "FROM ajb_xmba ax\n" + "JOIN (\n" + "\tSELECT ds.PROID\n"
            + "\tFROM ajb_dev_setup ds\n" + "\tJOIN ajb_dev_use du ON ds.GUID=du.setup_id\n"
            + "\tJOIN ajb_device d ON du.DEVID=d.GUID\n" + "\tLEFT JOIN ajb_dev_tear dt ON ds.GUID=dt.setup_id\n"
            + "\tWHERE (d.iscancel = 'N' OR d.iscancel IS NULL) AND ds.FSTATE='FL-P' AND du.FSTATE='TC-P' AND (dt.FSTATE!='FL-P' OR dt.GUID IS NULL)\n"
            + ") d ON ax.GUID=d.proid\n"
            + "WHERE (ax.prostate !='FINISH' OR ax.prostate IS NULL) AND ax.prostate != 'MIDDLE' AND ax.fstate IN ('TD-P','TC-P')\n"
            + "GROUP BY ax.ADDRESS_CITY\n" + "ORDER BY ax.ADDRESS_CITY";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement statement = parserManager.parse(new StringReader(sql));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tablesNames = tablesNamesFinder.getTableList(statement);
        tablesNames.forEach(System.out::println);
    }
}
