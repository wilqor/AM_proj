package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.model.ProductPriority;

/**
 * Created by Kuba on 2015-12-14.
 */
public class ProductsDataSource {
    private static final String TAG = "ProductsDataSource";

    private final SQLiteDatabase database;
    private String[] allColumns = {
            ProductsTable.COLUMN_LOCAL_ID,
            ProductsTable.COLUMN_ID,
            ProductsTable.COLUMN_DEVICE_QUANTITY,
            ProductsTable.COLUMN_QUANTITY,
            ProductsTable.COLUMN_NAME,
            ProductsTable.COLUMN_PRIORITY,
            ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP
    };

    public ProductsDataSource(SQLiteDatabase database) {
        this.database = database;
    }

    public Product createProduct(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_ID, 0);
        contentValues.put(ProductsTable.COLUMN_NAME, name);
        contentValues.put(ProductsTable.COLUMN_QUANTITY, 0);
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, 0);
        contentValues.put(ProductsTable.COLUMN_PRIORITY, ProductsTable.DEFAULT_PRIORITY.name());
        contentValues.put(ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP, 0);
        return createProductInternal(contentValues);
    }

    public Product createProductFromRemote(Product remote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_ID, remote.getId());
        contentValues.put(ProductsTable.COLUMN_NAME, remote.getName());
        contentValues.put(ProductsTable.COLUMN_QUANTITY, remote.getQuantity());
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, 0);
        contentValues.put(ProductsTable.COLUMN_PRIORITY, remote.getPriority().name());
        contentValues.put(ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP, remote.getPriorityUpdateTimestamp());
        return createProductInternal(contentValues);
    }

    public void updateProductQuantity(long localId, int quantity, int deviceQuantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_QUANTITY, quantity);
        contentValues.put(ProductsTable.COLUMN_DEVICE_QUANTITY, deviceQuantity);
        updateProductInternal(localId, contentValues);
    }

    public void updateProductPriority(long localId, ProductPriority productPriority) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_PRIORITY, productPriority.name());
        contentValues.put(ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP, DBUtils.getCurrentTimestamp());
        updateProductInternal(localId, contentValues);
    }

    public void updateProductFromRemote(Product updatedLocal) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductsTable.COLUMN_ID, updatedLocal.getId());
        contentValues.put(ProductsTable.COLUMN_QUANTITY, updatedLocal.getQuantity());
        contentValues.put(ProductsTable.COLUMN_PRIORITY, updatedLocal.getPriority().name());
        contentValues.put(ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP, updatedLocal.getPriorityUpdateTimestamp());
        updateProductInternal(updatedLocal.getLocalId(), contentValues);
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
                ProductsTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)}, null, null, null);
        cursor.moveToFirst();
        Product foundProduct = cursorToProduct(cursor);
        cursor.close();
        return foundProduct;
    }

    public boolean isProductNameUnique(String name) {
        Cursor cursor = database.query(ProductsTable.TABLE_PRODUCT, allColumns,
                ProductsTable.COLUMN_NAME + " = ? ", new String[]{String.valueOf(name)}, null, null, null);
        List<Product> products = getProductsFromQueriedCursor(cursor);
        return products.isEmpty();
    }

    public List<Product> getAllProducts() {
        Cursor cursor = database.query(ProductsTable.TABLE_PRODUCT, allColumns,
                null, null, null, null, null);
        return getProductsFromQueriedCursor(cursor);
    }

    public Product getProductForRemoteId(int remoteId) {
        Cursor cursor = database.query(ProductsTable.TABLE_PRODUCT, allColumns,
                ProductsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(remoteId)}, null, null, null);
        List<Product> found = getProductsFromQueriedCursor(cursor);
        if (found.isEmpty()) {
            return null;
        } else {
            return found.get(0);
        }
    }

    public List<Product> getAllProductsForTags(List<Long> tagLocalIds) {
        List<Product> products = new ArrayList<>();
        if (tagLocalIds.isEmpty()) {
            products.addAll(getAllProducts());
        } else {
            Set<Product> productSet = new HashSet<>();
            for (Long tag : tagLocalIds) {
                productSet.addAll(getAllProductsForTag(tag));
            }
            products.addAll(productSet);
        }
        return products;
    }

    public List<Product> getAllProductsForTag(long tagLocalId) {
        String rawQuery = "select * from " + ProductsTable.TABLE_PRODUCT
                + " where " + ProductsTable.COLUMN_LOCAL_ID + " in "
                + "(select " + ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID + " from "
                + ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION + " where "
                + ProductTagRelationTable.COLUMN_TAG_LOCAL_ID + " = " + tagLocalId
                + " and " + ProductTagRelationTable.COLUMN_IS_ACTIVE + " = " + DBUtils.booleanToInt(true)
                + ")";
        Cursor cursor = database.rawQuery(rawQuery, null);
        return getProductsFromQueriedCursor(cursor);
    }

    @NonNull
    private List<Product> getProductsFromQueriedCursor(Cursor cursor) {
        List<Product> products = new ArrayList<>();
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
        product.setLocalId(cursor.getLong(cursor.getColumnIndex(ProductsTable.COLUMN_LOCAL_ID)));
        product.setId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_ID)));
        product.setDeviceQuantity(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_DEVICE_QUANTITY)));
        product.setQuantity(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_QUANTITY)));
        product.setName(cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_NAME)));
        product.setPriority(Enum.valueOf(ProductPriority.class, cursor.getString(cursor.getColumnIndex(ProductsTable.COLUMN_PRIORITY))));
        product.setPriorityUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(ProductsTable.COLUMN_PRIORITY_UPDATE_TIMESTAMP)));
        return product;
    }
}
