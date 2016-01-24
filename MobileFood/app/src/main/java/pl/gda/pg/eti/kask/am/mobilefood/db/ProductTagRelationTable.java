package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kuba on 2016-01-20.
 */
public class ProductTagRelationTable {
    static final String TABLE_PRODUCT_TAG_RELATION = "product_tag";

    static final String COLUMN_LOCAL_ID = "_id";
    static final String COLUMN_PRODUCT_ID = "product_id";
    static final String COLUMN_PRODUCT_LOCAL_ID = "product_local_id";
    static final String COLUMN_TAG_ID = "tag_id";
    static final String COLUMN_TAG_LOCAL_ID = "tag_local_id";
    static final String COLUMN_IS_ACTIVE = "active";
    static final String COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP = "active_update_timestamp";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PRODUCT_TAG_RELATION
            + "("
            + COLUMN_LOCAL_ID + " integer primary key autoincrement, "
            + COLUMN_PRODUCT_ID + " integer, "
            + COLUMN_PRODUCT_LOCAL_ID + " integer, "
            + COLUMN_TAG_ID + " integer, "
            + COLUMN_TAG_LOCAL_ID + " integer, "
            + COLUMN_IS_ACTIVE + " integer, "
            + COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP + " integer"
            + ");";

    public static void onCreate(SQLiteDatabase database) { database.execSQL(DATABASE_CREATE); }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
                onCreate(database);
        }
    }
}
