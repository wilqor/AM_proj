package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.database.sqlite.SQLiteDatabase;

import pl.gda.pg.eti.kask.am.mobilefood.model.ProductPriority;

/**
 * Created by Kuba on 2015-12-14.
 */
public class ProductsTable {
    static final String TABLE_PRODUCT = "product";

    static final ProductPriority DEFAULT_PRIORITY = ProductPriority.MEDIUM;
    static final long DEFAULT_TIMESTAMP = 0;

    static final String DEFAULT_PRIORITY_STRING = "'" + DEFAULT_PRIORITY.name() + "'";
    static final String DEFAULT_PRIORITY_UPDATE_TIMESTAMP_STRING = "" + DEFAULT_TIMESTAMP;

    static final String COLUMN_LOCAL_ID = "_id";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_QUANTITY = "quantity";
    static final String COLUMN_DEVICE_QUANTITY = "device_quantity";
    static final String COLUMN_PRIORITY = "priority";
    static final String COLUMN_PRIORITY_UPDATE_TIMESTAMP = "priority_update_timestamp";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PRODUCT
            + "("
            + COLUMN_LOCAL_ID + " integer primary key autoincrement, "
            + COLUMN_ID + " integer, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_QUANTITY + " integer not null, "
            + COLUMN_DEVICE_QUANTITY + " integer not null, "
            + COLUMN_PRIORITY + " text default " + DEFAULT_PRIORITY_STRING + ","
            + COLUMN_PRIORITY_UPDATE_TIMESTAMP + " integer default " + DEFAULT_PRIORITY_UPDATE_TIMESTAMP_STRING
            + ");";
    private static final String DATABASE_UPGRADE_TO_V2_ADD_PRIORITY_COLUMN = "alter table "
            + TABLE_PRODUCT
            + " add column "
            + COLUMN_PRIORITY + " text default " + DEFAULT_PRIORITY_STRING;
    private static final String DATABASE_UPGRADE_TO_V2_ADD_PRIORITY_UPDATE_TIMESTAMP_COLUMN = "alter table "
            + TABLE_PRODUCT
            + " add column "
            + COLUMN_PRIORITY_UPDATE_TIMESTAMP + " integer default " + DEFAULT_PRIORITY_UPDATE_TIMESTAMP_STRING;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                database.execSQL(DATABASE_UPGRADE_TO_V2_ADD_PRIORITY_COLUMN);
                database.execSQL(DATABASE_UPGRADE_TO_V2_ADD_PRIORITY_UPDATE_TIMESTAMP_COLUMN);
        }
    }

}
