package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kuba on 2016-01-22.
 */
public class DBGetter {

    private final MySQLiteHelper helper;

    public DBGetter(Context context) {
        helper = new MySQLiteHelper(context);
    }

    public MySQLiteHelper getHelper() {
        return helper;
    }

    public SQLiteDatabase getDb() {
        return helper.getWritableDatabase();
    }
}
