package test.helper;

import org.junit.Test;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SQLBuilder {
    SQLBuilder() {
    }

    public static String buildInsert(String tableName, List<String> properties) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ")
                .append(tableName)
                .append(" (");

        boolean first = true;
        for (String prop: properties) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(SQLBuilder.toSnakeCase(prop));
        }

        sb.append(") values (");

        first = true;
        for (String prop: properties) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(":").append(prop);
        }
        sb.append(")");

        return sb.toString();
    }

    public static String buildInsert(String tableName, Object bean) {
        List<String> properties = listProperties(bean);

        return buildInsert(tableName, properties);
    }

    public static List<String> listProperties(Object bean) {
        List<String> properties = new ArrayList<String>();
        Class cl = bean.getClass();

        for (Field field : cl.getDeclaredFields()) {
            String name = field.getName();
            if (name.contains("$")) {
                continue;
            }

            properties.add(field.getName());
        }

        return properties;
    }

    private static Pattern camelPattern = Pattern.compile("([a-z])([A-Z])");

    public static String toSnakeCase(String camelCase) {
        Matcher m = camelPattern.matcher(camelCase);
        return m.replaceAll("$1_$2").toLowerCase();
    }

    public static String toCamelCase(String snakeCase) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (String s: snakeCase.split("_")) {
            if (first) {
                sb.append(s);
                first = false;
            }
            else {
                sb.append(Character.toUpperCase(s.charAt(0)));
                if (s.length() > 1) {
                    sb.append(s.substring(1, s.length()));
                }
            }
        }
        return sb.toString();
    }

}
