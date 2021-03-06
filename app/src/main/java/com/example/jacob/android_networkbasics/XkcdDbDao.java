package com.example.jacob.android_networkbasics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class XkcdDbDao {
    private static SQLiteDatabase db;

    static void initializeInstance(Context context) {
        if (db == null) {
            XkcdDbHelper helper = new XkcdDbHelper(context);
            db = helper.getWritableDatabase();
        }
    }

    static void createComic(XkcdComic xkcdComic) {
        if (db != null) {
            ContentValues values = new ContentValues();
            XkcdDbInfo info = xkcdComic.getXkcdDbInfo();
            values.put(XkcdDbContract.ComicEntry._ID, xkcdComic.getNum());
            values.put(XkcdDbContract.ComicEntry.COLUMN_NAME_TIMESTAMP, info.getTimestamp());
            values.put(XkcdDbContract.ComicEntry.COLUMN_NAME_FAVORITE, info.getFavorite());
            db.insert(XkcdDbContract.ComicEntry.TABLE_NAME, null, values);
        }
    }

    static XkcdDbInfo readComic(int id) {
//        SELECT * FROM comics WHERE comic_id=200;
        if (db != null) {
            Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s'",
                    XkcdDbContract.ComicEntry.TABLE_NAME,
                    XkcdDbContract.ComicEntry._ID,
                    id),
                    null);
            XkcdDbInfo xkcdDbInfo;
            int index;
            if (cursor.moveToNext() && (cursor.getCount() == 1)) {

                index = cursor.getColumnIndexOrThrow(XkcdDbContract.ComicEntry.COLUMN_NAME_TIMESTAMP);
                int timestamp = cursor.getInt(index);

                index = cursor.getColumnIndexOrThrow(XkcdDbContract.ComicEntry.COLUMN_NAME_FAVORITE);
                int favorite = cursor.getInt(index);

                xkcdDbInfo = new XkcdDbInfo(timestamp, favorite);
            } else {
                xkcdDbInfo = null;
            }
            cursor.close();
            return xkcdDbInfo;

        } else {
            return null;
        }
    }

    static void updateComic(XkcdComic xkcdComic) {
        if (db != null) {
//            String whereClause = null;
            String whereClause = String.format("%s = %s", XkcdDbContract.ComicEntry._ID, xkcdComic.getNum());
            final Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s WHERE %s",
                    XkcdDbContract.ComicEntry.TABLE_NAME,
                    whereClause),
                    null);
            if (cursor.getCount() == 1) {
                ContentValues values = new ContentValues();
                values.put(XkcdDbContract.ComicEntry.COLUMN_NAME_TIMESTAMP, xkcdComic.getXkcdDbInfo().getTimestamp());
                values.put(XkcdDbContract.ComicEntry.COLUMN_NAME_FAVORITE, xkcdComic.getXkcdDbInfo().getFavorite());

                db.update(XkcdDbContract.ComicEntry.TABLE_NAME, values, whereClause, null);
            }
            cursor.close();
        }
    }

    public static void deleteComic(int id) {
        if (db != null) {
            String whereClause = String.format("%s = '%s'",
                    XkcdDbContract.ComicEntry._ID,
                    id);
            final Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s WHERE %s",
                    XkcdDbContract.ComicEntry.TABLE_NAME,
                    whereClause),
                    null);
            if (cursor.getCount() == 1) {
                db.delete(XkcdDbContract.ComicEntry.TABLE_NAME, whereClause, null);
            }
            cursor.close();
        }
    }

    static ArrayList<Integer> readFavorites() {
        ArrayList<Integer> comicIds = new ArrayList<>();
        if (db != null) {
            Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s'",
                    XkcdDbContract.ComicEntry.TABLE_NAME,
                    XkcdDbContract.ComicEntry.COLUMN_NAME_FAVORITE,
                    1),
                    null);
            int index;
            while (cursor.moveToNext()) {
                index = cursor.getColumnIndexOrThrow(XkcdDbContract.ComicEntry._ID);
                comicIds.add(cursor.getInt(index));
            }
            cursor.close();
        }
        return comicIds;
    }
}
