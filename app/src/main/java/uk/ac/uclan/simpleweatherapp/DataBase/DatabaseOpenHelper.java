package uk.ac.uclan.simpleweatherapp.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Vasco on 10/03/2016.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    // this is used to name the underlying file storing the actual data
    public static final String DATABASE_NAME = "blog_entries.db";
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BLOG_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // in our case, we simply delete all data and recreate the DB
        db.execSQL(SQL_DELETE_BLOG_ENTRIES);
        onCreate(db);
    }
    private static final String SQL_CREATE_BLOG_ENTRIES =
            "CREATE TABLE entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, "+
                    "body TEXT NOT NULL "+
                    ")";
    private static final String SQL_DELETE_BLOG_ENTRIES =
            "DROP TABLE IF EXISTS entries";
}
