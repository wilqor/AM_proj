package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kuba on 2016-01-20.
 */
public class TagsTable {
    static final String TABLE_TAG = "tag";

    static final String COLUMN_LOCAL_ID = "_id";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TAG
            + "("
            + COLUMN_LOCAL_ID + " integer primary key autoincrement, "
            + COLUMN_ID + " integer, "
            + COLUMN_NAME + " text"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
                onCreate(database);
        }
    }
}
