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

import java.io.File;
import java.sql.*;

/**
 * The SQLite database itself.
 * This object allows ease of querying or executing commands.
 * <p>
 * Will handle establishing the connection, managing statements, and closing the connection
 * to the database by utilizing the SQLite JDBC drivers.
 * <p>
 * The following operations are supported:
 * (1) Insert data into the database. {@link #insert(String, ContentValues)}
 * (2) Update data in the database. {@link #update(String, ContentValues, String)}
 * (3) Delete data in the database. {@link #delete(String, String)}
 * (4) Query data in the database. {@link #query(String, String, String, String)}
 * (5) Raw query data in the database. {@link #rawQuery(String)}
 * (6) Raw command on the database. {@link #execSQL(String)}
 *
 * @author Tyler Suehr
 */
public final class SQLiteDatabase extends SQLiteCloseable {
    public static final int OPEN_READWRITE = 0x00000000;          // update native code if changing
    public static final int OPEN_READONLY = 0x00000001;           // update native code if changing

    /**
     * When a constraint violation occurs, an immediate ROLLBACK occurs,
     * thus ending the current transaction, and the command aborts with a
     * return code of SQLITE_CONSTRAINT. If no transaction is active
     * (other than the implied transaction that is created on every command)
     * then this algorithm works the same as ABORT.
     */
    public static final int CONFLICT_ROLLBACK = 1;
    /**
     * When a constraint violation occurs,no ROLLBACK is executed
     * so changes from prior commands within the same transaction
     * are preserved. This is the default behavior.
     */
    public static final int CONFLICT_ABORT = 2;
    /**
     * When a constraint violation occurs, the command aborts with a return
     * code SQLITE_CONSTRAINT. But any changes to the database that
     * the command made prior to encountering the constraint violation
     * are preserved and are not backed out.
     */
    public static final int CONFLICT_FAIL = 3;
    /**
     * When a constraint violation occurs, the one row that contains
     * the constraint violation is not inserted or changed.
     * But the command continues executing normally. Other rows before and
     * after the row that contained the constraint violation continue to be
     * inserted or updated normally. No error is returned.
     */
    public static final int CONFLICT_IGNORE = 4;
    /**
     * When a UNIQUE constraint violation occurs, the pre-existing rows that
     * are causing the constraint violation are removed prior to inserting
     * or updating the current row. Thus the insert or update always occurs.
     * The command continues executing normally. No error is returned.
     * If a NOT NULL constraint violation occurs, the NULL value is replaced
     * by the default value for that column. If the column has no default
     * value, then the ABORT algorithm is used. If a CHECK constraint
     * violation occurs then the IGNORE algorithm is used. When this conflict
     * resolution strategy deletes rows in order to satisfy a constraint,
     * it does not invoke delete triggers on those rows.
     * This behavior might change in a future release.
     */
    public static final int CONFLICT_REPLACE = 5;
    /**
     * Use the following when no conflict action is specified.
     */
    public static final int CONFLICT_NONE = 0;

    public static final String[] CONFLICT_VALUES = new String[]
            {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};

    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String PATH = "jdbc:sqlite:";
    private Connection connection;
    private Statement statement;
    private Boolean isOpen;


    SQLiteDatabase(String dbName) {
        openConnection(dbName);
    }

    @Override
    protected void onAllReferencesReleased() {
        try {
            if (statement != null) {
                this.statement.close();
            }
            if (connection != null) {
                this.connection.close();
            }
            System.out.println("All references released!");
        } catch (SQLException ex) {
            logException(ex);
        }
    }

    /**
     * Queries data from the SQLite database.
     *
     * @param table     the name of the table to query
     * @param selection the WHERE clause (i.e. "[id]=12")
     * @param order     the ORDER BY clause (i.e. "[timestamp ASC]")
     * @param limit     the LIMIT clause (i.e. "4")
     * @return the results
     */
    public ResultSet query(String table, String selection, String order, String limit) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createQuery(table, selection, order, limit);
            return statement.executeQuery(SQL);
        } catch (SQLException ex) {
            logException(ex);
            return null;
        } finally {
            releaseReference();
        }
    }

    /**
     * Queries data from the SQLite database.
     *
     * @param table     the name of the table to query
     * @param cols      the columns to select from the table
     * @param selection the WHERE clause (i.e. "[id]=12")
     * @param order     the ORDER BY clause (i.e. "[timestamp ASC]")
     * @param limit     the LIMIT clause (i.e. "4")
     * @return the results
     */
    public ResultSet query(String table, String[] cols, String selection, String order, String limit) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createQuery(table, cols, selection, order, limit);
            return statement.executeQuery(SQL);
        } catch (SQLException ex) {
            logException(ex);
            return null;
        } finally {
            releaseReference();
        }
    }

    /**
     * Convenience method for inserting data into the SQLite database.
     *
     * @param table  the name of the table
     * @param values the content to be inserted
     */
    public void insert(String table, ContentValues values) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createInsert(table, values);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
    }

    /**
     * Convenience method for updating data in the SQLite database.
     *
     * @param table     the name of the table
     * @param values    the content to be updated
     * @param selection the WHERE clause
     */
    public void update(String table, ContentValues values, String selection) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createUpdate(table, values, selection);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
    }

    /**
     * Convenience method for updating data in the SQLite database.
     *
     * @param table     the name of the table
     * @param values    the content to be updated
     * @param selection the WHERE clause
     * @return
     */
    public Integer update(String table, ContentValues values, String selection, String[] args) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createUpdate(table, values, selection);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
        return 0;
    }

    /**
     * Convenience method for deleting data in the SQLite database.
     *
     * @param table     the name of the table
     * @param selection the WHERE clause
     */
    public void delete(String table, String selection) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createDelete(table, selection);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
    }

    public int delete(String table, String where, String[] args) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createDelete(table, where, args);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
        return 0;
    }

    /**
     * Queries data from the SQLite database using a raw SQL query.
     *
     * @param sql the SQL query to run
     * @return the results
     */
    public ResultSet rawQuery(String sql) {
        acquireReference();
        try {
            return statement.executeQuery(sql);
        } catch (SQLException ex) {
            logException(ex);
            return null;
        } finally {
            releaseReference();
        }
    }

    /**
     * Executes a command on the SQLite database using a raw SQL query.
     *
     * @param sql the SQL query to run
     */
    public void execSQL(String sql) {
        acquireReference();
        try {
            this.statement.executeUpdate(sql);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
    }

    /**
     * Gets the user version of the SQLite database.
     *
     * @return the user version of the database
     */
    int getVersion() {
        acquireReference();
        try {
            final String SQL = "PRAGMA user_version";
            ResultSet c = statement.executeQuery(SQL);
            return c.getInt("user_version");
        } catch (SQLException ex) {
            logException(ex);
            return -1;
        } finally {
            releaseReference();
        }
    }

    /**
     * Sets the user version of the SQLite database.
     *
     * @param version the user version to be set
     */
    void setVersion(int version) {
        acquireReference();
        try {
            final String SQL = "PRAGMA user_version=" + version;
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
    }

    /**
     * Opens a connection to the SQLite database.
     *
     * @param dbName the name of the database file (don't include file extension)
     */
    private void openConnection(String dbName) {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(PATH + dbName);
            this.connection.setAutoCommit(false);
            this.statement = connection.createStatement();
            acquireReference();
            isOpen = true;
        } catch (ClassNotFoundException | SQLException ex) {
            logException(ex);
        }
    }

    public void deleteDatabase(String dbName) {
        close();
        File file = new File(dbName);
        file.delete();
    }

    /**
     * Convenience method to log an exception and print its stacktrace.
     *
     * @param ex the exception
     */
    private void logException(final Exception ex) {
        System.err.println("SQLite > " + ex.getMessage());
        ex.printStackTrace();
    }

    public Boolean isReadOnly() {
        return false;
    }

    public Boolean isOpen() {
        return isOpen;
    }

    public boolean inTransaction() {
        return false;
    }

    public ResultSet rawQuery(String sql, String[] args) {
        return rawQuery(sql);
    }

    public long insertWithOnConflict(String table, ContentValues values, int conflictAlgorithm) {
        acquireReference();
        try {
            final String SQL = SQLBuilder.createInsertWithOnConflict(table, values, conflictAlgorithm);
            this.statement.executeUpdate(SQL);
            this.connection.commit();
        } catch (SQLException ex) {
            logException(ex);
        } finally {
            releaseReference();
        }
        return 0;
    }

    public long insertOrThrow(String table, ContentValues values) {
        return insertWithOnConflict(table, values, CONFLICT_NONE);
    }
}