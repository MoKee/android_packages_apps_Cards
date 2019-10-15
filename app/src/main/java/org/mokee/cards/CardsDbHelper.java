/*
 * Copyright (C) 2019 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mokee.cards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class CardsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "CardsDbHelper";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "cards.db";

    private static final String TABLE_NAME = "cards";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Columns.UID + " BLOB," +
            Columns.NAME + " TEXT," +
            Columns.COLOR + " INTEGER," +
            Columns.TEXTURE + " BLOB)";

    CardsDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    List<Card> query() {
        final List<Card> cards = new ArrayList<>();

        try (final SQLiteDatabase db = getReadableDatabase();
             final Cursor cursor = db.query(TABLE_NAME, null, null, null,
                     null, null, null)) {
            while (cursor.moveToNext()) {
                final Card card = new Card();
                card.id = cursor.getInt(cursor.getColumnIndexOrThrow(Columns._ID));
                card.uid = cursor.getBlob(cursor.getColumnIndexOrThrow(Columns.UID));
                card.name = cursor.getString(cursor.getColumnIndexOrThrow(Columns.NAME));
                card.color = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.COLOR));
                card.texture = cursor.getBlob(cursor.getColumnIndexOrThrow(Columns.TEXTURE));
                cards.add(card);
            }
        }

        return cards;
    }

    boolean insert(byte[] uid, String name, @ColorInt int color, @Nullable byte[] texture) {
        final ContentValues values = new ContentValues();
        values.put(Columns.UID, uid);
        values.put(Columns.NAME, name);
        values.put(Columns.COLOR, color);
        values.put(Columns.TEXTURE, texture != null ? texture : new byte[]{});

        try (final SQLiteDatabase db = getWritableDatabase()) {
            db.insertOrThrow(TABLE_NAME, null, values);
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "Failed inserting new card", e);
            return false;
        }
    }

    boolean update(@NonNull Card card) {
        final ContentValues values = new ContentValues();
        values.put(Columns.NAME, card.name);
        values.put(Columns.COLOR, card.color);
        values.put(Columns.TEXTURE, card.texture != null ? card.texture : new byte[]{});

        try (final SQLiteDatabase db = getWritableDatabase()) {
            final String selection = Columns._ID + " = ?";
            final String[] selectionArgs = {String.valueOf(card.id)};
            db.update(TABLE_NAME, values, selection, selectionArgs);
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "Failed inserting new card", e);
            return false;
        }
    }

    boolean delete(int id) {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            final String selection = Columns._ID + " = ?";
            final String[] selectionArgs = {String.valueOf(id)};
            db.delete(TABLE_NAME, selection, selectionArgs);
            return true;
        } catch (SQLException e) {
            Log.e(TAG, "Failed inserting new card", e);
            return false;
        }
    }

    interface Columns extends BaseColumns {
        String UID = "uid";
        String NAME = "name";
        String COLOR = "color";
        String TEXTURE = "texture";
    }

    class Card {
        int id;

        byte[] uid;

        String name;

        @ColorInt
        int color;

        byte[] texture;
    }

}
