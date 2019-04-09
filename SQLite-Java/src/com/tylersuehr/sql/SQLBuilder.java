/*
 * MIT License
 *
 * Copyright (c) Tyler Suehr 2019.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tylersuehr.sql;

import static com.tylersuehr.sql.SQLiteDatabase.CONFLICT_VALUES;

/**
 * Utility to help construct SQL queries and commands.
 *
 * @author Tyler Suehr
 */
final class SQLBuilder {
    // SELECT * FROM [table] WHERE [col1] = value ORDER BY [col] LIMIT 0;
    static String createQuery(String table, String selection, String order, String limit) {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append("[").append(table).append("]");
        sb.append(selection != null ? " WHERE " + selection : "");
        sb.append(order != null ? " ORDER BY " + order : "");
        sb.append(limit != null ? " LIMIT " + limit : "");
        sb.append(";");
        return sb.toString();
    }

    // SELECT ([col1],[col2],[col3]) FROM [table] WHERE [col] = value ORDER BY [col] LIMIT 0;
    static String createQuery(String table, String[] cols, String selection, String order, String limit) {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (cols != null) {
            //sb.append("(");
            int i = 0;
            for (String c : cols) {
                sb.append((i > 0) ? "," : "");
                //sb.append("[").append(c).append("]");
                sb.append(c);
                i++;
            }
            //sb.append(") FROM ");
            sb.append(" FROM ");
        } else {
            sb.append("* FROM ");
        }
        //sb.append("[").append(table).append("]");
        sb.append(table);

        sb.append(selection != null ? " WHERE " + selection : "");
        sb.append(order != null ? " ORDER BY " + order : "");
        sb.append(limit != null ? " LIMIT " + limit : "");
        sb.append(";");
        return sb.toString();
    }

    // INSERT INTO [table] ([col1],[col2],[col3],[col4]) VALUES ('test', 'test2', 123, 12.123);
    static String createInsert(String table, ContentValues values) {
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append("[").append(table).append("] (");

        int i = 0;
        for (String col : values.getKeys()) {
            sb.append((i > 0) ? "," : "");
            sb.append("[").append(col).append("]");
            i++;
        }
        sb.append(") VALUES (");

        i = 0;
        for (Object o : values.getData()) {
            sb.append((i > 0) ? "," : "");
            if (o instanceof String) {
                sb.append("'").append(o).append("'");
            } else {
                sb.append(o);
            }
            i++;
        }
        sb.append(");");
        return sb.toString();
    }

    // UPDATE [table] SET [col1] = 'test' WHERE [col2] = 3;
    static String createUpdate(String table, ContentValues values, String selection) {
        final StringBuilder sb = new StringBuilder(120);
        sb.append("UPDATE ");
        sb.append("[").append(table).append("]");
        sb.append(" SET ");

        int i = 0;
        for (String col : values.getKeys()) {
            sb.append((i > 0) ? "," : "");
            sb.append("[").append(col).append("]");
            sb.append("=");

            if (values.get(col) instanceof String) {
                sb.append("'").append(values.get(col)).append("'");
            } else {
                sb.append(values.get(col));
            }
            i++;
        }

        sb.append(selection != null ? " WHERE " + selection : "");
        sb.append(";");
        return sb.toString();
    }

    // DELETE FROM [table] WHERE [col1] = 23;
    static String createDelete(String table, String selection) {
        final StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append("[").append(table).append("]");
        sb.append(selection != null ? " WHERE " + selection : "");
        sb.append(";");
        return sb.toString();
    }

    static String createDelete(String table, String where, String[] whereArgs) {
        final StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append("[").append(table).append("]");
        sb.append(where != null ? " WHERE " + where : "");
        for (String whereArg : whereArgs) {
            sb.append(" = " + whereArg);
        }
        sb.append(";");
        return sb.toString();
    }

    public static String createInsertWithOnConflict(String table, ContentValues values, int conflictAlgorithm) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');
        Object[] bindArgs = null;
        int size = (values != null && !values.isEmpty())
                ? values.size() : 0;
        if (size > 0) {
            bindArgs = new Object[size];
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName);
                bindArgs[i++] = values.get(colName);
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? ",?" : "?");
            }
        }
        sql.append(')');
        return sql.toString();
    }
}