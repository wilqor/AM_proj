package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.model.Product;

/**
 * Created by Kuba on 2015-12-14.
 */
public class ProductsDataSource {

    private static final String TAG = "ProductsDataSource";

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            ProductsTable.COLUMN_LOCAL_ID,
            ProductsTable.COLUMN_ID,
            ProductsTable.COLUMN_DEVICE_QUANTITY,
            ProductsTable.COLUMN_QUANTITY,
            ProductsTable.COLUMN_NAME
    };

    public ProductsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Product createProduct(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_ID, 0);
        contentValues.put(ProductsTable.COLUMN_NAME, name);
        contentValues.put(ProductsTable.COLUMN_QUANTITY, 0);
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, 0);
        return createProductInternal(contentValues);
    }

    public Product createProductFromRemote(String name, Integer remoteId, int remoteQuantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_NAME, name);
        contentValues.put(ProductsTable.COLUMN_ID, remoteId);
        contentValues.put(ProductsTable.COLUMN_QUANTITY, remoteQuantity);
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, 0);
        return createProductInternal(contentValues);
    }

    public void updateProduct(long localId, int quantity, int deviceQuantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_QUANTITY, quantity);
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, deviceQuantity);
        updateProductInternal(localId, contentValues);
    }

    public void updateProductFromRemote(long localId, Integer remoteId, int remoteQuantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_ID, remoteId);
        contentValues.put(ProductsTable.COLUMN_QUANTITY, remoteQuantity);
        updateProductInternal(localId, contentValues);
    }

    @NonNull
    private Product createProductInternal(ContentValues contentValues) {
        long insertId = database.insert(ProductsTable.TABLE_PRODUCT, null, contentValues);
        Product created = getProduct(insertId);
        Log.d(TAG, "Created product: " + created);
        return created;
    }

    private void updateProductInternal(long localId, ContentValues contentValues) {
        database.update(ProductsTable.TABLE_PRODUCT, contentValues,
                ProductsTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)});
    }

    @NonNull
    public Product getProduct(long localId) {
        Cursor cursor = database.query(ProductsTable.TABLE_PRODUCT, allColumns,
                ProductsTable.COLUMN_LOCAL_ID + " = " + localId, null, null, null, null);
        cursor.moveToFirst();
        Product foundProduct = cursorToProduct(cursor);
        cursor.close();
        return foundProduct;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        Cursor cursor = database.query(ProductsTable.TABLE_PRODUCT, allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Product product = cursorToProduct(cursor);
            products.add(product);
            cursor.moveToNext();
        }
        cursor.close();
        return products;
    }

    public void deleteProduct(long localId) {
        database.delete(ProductsTable.TABLE_PRODUCT,
                ProductsTable.COLUMN_LOCAL_ID + " = " + localId, null);
        Log.d(TAG, "Deleted product id: " + localId);
    }

    @NonNull
    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setLocalId(cursor.getLong(0));
        product.setId(cursor.getInt(1));
        product.setDeviceQuantity(cursor.getInt(2));
        product.setQuantity(cursor.getInt(3));
        product.setName(cursor.getString(4));
        return product;
    }
}
