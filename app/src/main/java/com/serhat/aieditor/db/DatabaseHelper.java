package com.serhat.aieditor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.serhat.aieditor.Utils;
import com.serhat.aieditor.model.GalleryModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "gallery_db";
    private final String TABLE_SAVED = "tbl_saved";
    private final String TABLE_HISTORY = "tbl_history";
    private final String TABLE_UPSCALED = "tbl_upscaled";
    private final String KEY_PROMPT = "prompt";
    private final String KEY_NEGATIVE_PROMPT = "negative_prompt";
    private final String KEY_WIDTH = "width";
    private final String KEY_HEIGHT = "height";
    private final String KEY_PATH = "path";
    private final String KEY_ORIGINAL_PATH = "original_path";
    private final String KEY_SEED = "seed";
    private final String KEY_ADDED_DATE = "addedDate";
    private final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS" +
                    " tbl_history(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "prompt TEXT,negative_prompt TEXT,width INTEGER," +
                    "height INTEGER,path TEXT,original_path TEXT,seed TEXT,addedDate DATETIME)");
            db.execSQL("CREATE TABLE IF NOT EXISTS" +
                    " tbl_saved(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "prompt TEXT,negative_prompt TEXT,width INTEGER," +
                    "height INTEGER,path TEXT,original_path TEXT,seed TEXT,addedDate DATETIME)");
            db.execSQL("CREATE TABLE IF NOT EXISTS" +
                    " tbl_upscaled(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "prompt TEXT,negative_prompt TEXT,width INTEGER," +
                    "height INTEGER,path TEXT,original_path TEXT,seed TEXT,addedDate DATETIME)");

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS tbl_history");
            db.execSQL("DROP TABLE IF EXISTS tbl_saved");
            db.execSQL("DROP TABLE IF EXISTS tbl_upscaled");
            onCreate(db);
        } catch (Exception ignored) {

        }
    }


    private String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public void deleteAllHistory() {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.delete("tbl_history", null, null);
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }

    }

    public void deleteAllBookmarks() {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.delete("tbl_saved", null, null);
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();

            }
        }
    }

    public void deleteAllUpscaled() {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.delete("tbl_saved", null, null);
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();

            }
        }
    }


    public void addHistory(GalleryModel galleryModel) {
        SQLiteDatabase writableDatabase = null;
        try {
            if (TextUtils.isEmpty(galleryModel.path)) {
                return;
            }
            writableDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_PROMPT, galleryModel.prompt);
            contentValues.put(KEY_NEGATIVE_PROMPT, galleryModel.negative_prompt);
            contentValues.put(KEY_WIDTH, galleryModel.width);
            contentValues.put(KEY_HEIGHT, galleryModel.height);
            contentValues.put(KEY_PATH, galleryModel.path);
            contentValues.put(KEY_ORIGINAL_PATH, galleryModel.originalPath);
            contentValues.put(KEY_SEED, galleryModel.seed);
            contentValues.put(KEY_ADDED_DATE, getDateTime());
            galleryModel.id = writableDatabase.insert(TABLE_HISTORY, null, contentValues);
        } catch (Exception ignored) {
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public List<GalleryModel> getAllHistory() {
        Cursor rawQuery = null;
        try {
            ArrayList<GalleryModel> arrayList = new ArrayList<>();
            rawQuery = getWritableDatabase().rawQuery(
                    "SELECT * FROM tbl_history ORDER BY addedDate DESC", null
            );
            if (rawQuery.moveToFirst()) {
                do {
                    GalleryModel galleryModel = new GalleryModel(rawQuery.getLong(0),
                            rawQuery.getString(1), rawQuery.getString(2),
                            rawQuery.getInt(3), rawQuery.getInt(4),
                            rawQuery.getString(5), rawQuery.getString(6),
                            rawQuery.getString(7), rawQuery.getString(8));
                    arrayList.add(galleryModel);
                } while (rawQuery.moveToNext());
            }
            return arrayList;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }

    public void deleteHistoryByID(long j) {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.delete(TABLE_HISTORY,
                    "id = ?", new String[]{String.valueOf(j)});
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public GalleryModel getHistory(String str) {
        Cursor rawQuery = null;
        try {
            GalleryModel galleryModel = null;
            if (TextUtils.isEmpty(str)) {
                return null;
            }

            rawQuery =
                    getWritableDatabase().rawQuery("SELECT * FROM tbl_history WHERE path = '" + str + "'", null);
            if (rawQuery.moveToFirst()) {
                galleryModel = new GalleryModel(rawQuery.getLong(0),
                        rawQuery.getString(1), rawQuery.getString(2),
                        rawQuery.getInt(3), rawQuery.getInt(4),
                        rawQuery.getString(5), rawQuery.getString(6),
                        rawQuery.getString(7), rawQuery.getString(8));
            }
            return galleryModel;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }

    public void addSaved(GalleryModel galleryModel) {
        SQLiteDatabase writableDatabase = null;
        try {
            if (TextUtils.isEmpty(galleryModel.path)) {
                return;
            }
            writableDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_PROMPT, galleryModel.prompt);
            contentValues.put(KEY_NEGATIVE_PROMPT, galleryModel.negative_prompt);
            contentValues.put(KEY_WIDTH, galleryModel.width);
            contentValues.put(KEY_HEIGHT, galleryModel.height);
            contentValues.put(KEY_PATH, galleryModel.path);
            contentValues.put(KEY_ORIGINAL_PATH, galleryModel.originalPath);
            contentValues.put(KEY_SEED, galleryModel.seed);
            contentValues.put(KEY_ADDED_DATE, getDateTime());
            galleryModel.id = writableDatabase.insert(TABLE_SAVED, null, contentValues);
        } catch (Exception ignored) {
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public List<GalleryModel> getAllSaved() {
        Cursor rawQuery = null;
        try {
            ArrayList<GalleryModel> arrayList = new ArrayList<>();
            rawQuery = getWritableDatabase().rawQuery(
                    "SELECT * FROM tbl_saved ORDER BY addedDate DESC", null
            );
            if (rawQuery.moveToFirst()) {
                do {
                    GalleryModel galleryModel = new GalleryModel(rawQuery.getLong(0),
                            rawQuery.getString(1), rawQuery.getString(2),
                            rawQuery.getInt(3), rawQuery.getInt(4),
                            rawQuery.getString(5), rawQuery.getString(6),
                            rawQuery.getString(7), rawQuery.getString(8));
                    arrayList.add(galleryModel);
                } while (rawQuery.moveToNext());
            }
            return arrayList;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }

    public void deleteSavedByID(long j) {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.delete(TABLE_SAVED,
                    "id = ?", new String[]{String.valueOf(j)});
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public GalleryModel getSaved(String str) {
        Cursor rawQuery = null;
        try {
            GalleryModel galleryModel = null;
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            rawQuery =
                    getWritableDatabase().rawQuery("SELECT * FROM tbl_saved WHERE path = '" + str + "'", null);
            if (rawQuery.moveToFirst()) {
                galleryModel = new GalleryModel(rawQuery.getLong(0),
                        rawQuery.getString(1), rawQuery.getString(2),
                        rawQuery.getInt(3), rawQuery.getInt(4),
                        rawQuery.getString(5), rawQuery.getString(6),
                        rawQuery.getString(7), rawQuery.getString(8));
            }
            return galleryModel;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }


    public void addUpscaled(GalleryModel galleryModel) {
        SQLiteDatabase writableDatabase = null;
        try {
            if (TextUtils.isEmpty(galleryModel.path)) {
                return;
            }
            writableDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_PROMPT, galleryModel.prompt);
            contentValues.put(KEY_NEGATIVE_PROMPT, galleryModel.negative_prompt);
            contentValues.put(KEY_WIDTH, galleryModel.width);
            contentValues.put(KEY_HEIGHT, galleryModel.height);
            contentValues.put(KEY_PATH, galleryModel.path);
            contentValues.put(KEY_ORIGINAL_PATH, galleryModel.originalPath);
            contentValues.put(KEY_SEED, galleryModel.seed);
            contentValues.put(KEY_ADDED_DATE, getDateTime());
            galleryModel.id = writableDatabase.insert(TABLE_UPSCALED, null, contentValues);
        } catch (Exception ignored) {
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public List<GalleryModel> getAllUpscaled() {
        Cursor rawQuery = null;
        try {
            ArrayList<GalleryModel> arrayList = new ArrayList<>();
            rawQuery = getWritableDatabase().rawQuery(
                    "SELECT * FROM tbl_upscaled ORDER BY addedDate DESC", null
            );
            if (rawQuery.moveToFirst()) {
                do {
                    GalleryModel galleryModel = new GalleryModel(rawQuery.getLong(0),
                            rawQuery.getString(1), rawQuery.getString(2),
                            rawQuery.getInt(3), rawQuery.getInt(4),
                            rawQuery.getString(5), rawQuery.getString(6),
                            rawQuery.getString(7), rawQuery.getString(8));
                    arrayList.add(galleryModel);
                } while (rawQuery.moveToNext());
            }
            return arrayList;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }

    public int deleteUpscaledByID(long j) {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            return writableDatabase.delete(TABLE_UPSCALED,
                    "id = ?", new String[]{String.valueOf(j)});
        } catch (Exception e) {
            return -1;
        } finally {
            if (writableDatabase != null && writableDatabase.isOpen()) {
                writableDatabase.close();
            }
        }
    }

    public GalleryModel getUpscaled(String str) {
        Cursor rawQuery = null;
        try {
            GalleryModel galleryModel = null;
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            rawQuery =
                    getWritableDatabase().rawQuery("SELECT * FROM tbl_upscaled WHERE path = '" + str + "'", null);
            if (rawQuery.moveToFirst()) {
                galleryModel = new GalleryModel(rawQuery.getLong(0),
                        rawQuery.getString(1), rawQuery.getString(2),
                        rawQuery.getInt(3), rawQuery.getInt(4),
                        rawQuery.getString(5), rawQuery.getString(6),
                        rawQuery.getString(7), rawQuery.getString(8));
            }
            return galleryModel;
        } catch (Exception e) {
            return null;
        } finally {
            if (rawQuery != null && !rawQuery.isClosed()) {
                rawQuery.close();
            }
        }
    }


}
