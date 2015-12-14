package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kuba on 2015-12-14.
 */
public class ProductsTable {

    static final String TABLE_PRODUCT = "product";

    static final String COLUMN_LOCAL_ID = "_id";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_QUANTITY = "quantity";
    static final String COLUMN_DEVICE_QUANTITY = "device_quantity";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PRODUCT
            + "("
            + COLUMN_LOCAL_ID + " integer primary key autoincrement, "
            + COLUMN_ID + " integer, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_QUANTITY + " integer not null, "
            + COLUMN_DEVICE_QUANTITY + " integer not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

}
