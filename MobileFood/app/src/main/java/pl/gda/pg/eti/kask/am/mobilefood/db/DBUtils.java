package pl.gda.pg.eti.kask.am.mobilefood.db;

/**
 * Created by Kuba on 2016-01-22.
 */
public class DBUtils {
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean intToBoolean(int dbBoolean) {
        return dbBoolean > 0;
    }
}
