/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import bolts.Task;
import com.tylersuehr.sql.SQLiteDatabase;
import com.tylersuehr.sql.SQLiteOpenHelper;

abstract class ParseSQLiteOpenHelper {

    private final SQLiteOpenHelper helper;

    public ParseSQLiteOpenHelper(String name, int version) {
        helper = new SQLiteOpenHelper(name, version) {
            @Override
            protected void onCreate(SQLiteDatabase db) {
                ParseSQLiteOpenHelper.this.onCreate(db);
            }

            @Override
            protected void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
                ParseSQLiteOpenHelper.this.onUpgrade(db, oldVersion, newVersion);
            }
        };
    }

    public Task<ParseSQLiteDatabase> getReadableDatabaseAsync() {
        return getDatabaseAsync(false);
    }

    public Task<ParseSQLiteDatabase> getWritableDatabaseAsync() {
        return getDatabaseAsync(true);
    }

    private Task<ParseSQLiteDatabase> getDatabaseAsync(final boolean writable) {
        return ParseSQLiteDatabase.openDatabaseAsync(
                helper, !writable ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
    }

    public void onOpen(SQLiteDatabase db) {
        // do nothing
    }

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public void deleteDatabase(String dbName) {
        helper.getWritableInstance().deleteDatabase(dbName);
    }
}
