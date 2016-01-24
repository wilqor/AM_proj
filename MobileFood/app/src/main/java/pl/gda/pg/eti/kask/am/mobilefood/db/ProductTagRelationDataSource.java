package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.model.ProductTagRelation;
import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;

/**
 * Created by Kuba on 2016-01-23.
 */
public class ProductTagRelationDataSource {
    private static final String TAG = "ProductTagRelationDS";

    private final SQLiteDatabase database;
    private String[] allColumns = {
            ProductTagRelationTable.COLUMN_LOCAL_ID,
            ProductTagRelationTable.COLUMN_PRODUCT_ID,
            ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID,
            ProductTagRelationTable.COLUMN_TAG_ID,
            ProductTagRelationTable.COLUMN_TAG_LOCAL_ID,
            ProductTagRelationTable.COLUMN_IS_ACTIVE,
            ProductTagRelationTable.COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP
    };

    public ProductTagRelationDataSource(SQLiteDatabase database) {
        this.database = database;
    }

    public ProductTagRelation getRelation(long localId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)}, null, null, null);
        cursor.moveToFirst();
        ProductTagRelation found = cursorToRelation(cursor);
        cursor.close();
        return found;
    }

    public ProductTagRelation getRelation(long localProductId, long localTagId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID + " = ? "
                        + " AND " + ProductTagRelationTable.COLUMN_TAG_LOCAL_ID + " = ? ",
                new String[]{String.valueOf(localProductId), String.valueOf(localTagId)}, null, null, null);
        List<ProductTagRelation> queryResult = getProductTagRelationsFromQueriedCursor(cursor);
        ProductTagRelation result;
        if (queryResult.isEmpty()) {
            result = null;
        } else {
            result = queryResult.get(0);
        }
        return result;
    }

    public List<ProductTagRelation> getAllRelations() {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                null, null, null, null, null);
        return getProductTagRelationsFromQueriedCursor(cursor);
    }

    public List<ProductTagRelation> getAllRelationsForTag(long localTagId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_TAG_LOCAL_ID + " = ? ", new String[]{String.valueOf(localTagId)}, null, null, null);
        return getProductTagRelationsFromQueriedCursor(cursor);
    }

    public List<ProductTagRelation> getAllRelationsForProduct(long localProductId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID + " = ? ", new String[]{String.valueOf(localProductId)}, null, null, null);
        return getProductTagRelationsFromQueriedCursor(cursor);
    }

    public List<ProductTagRelation> getAllActiveRelationsForProduct(long localProductId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID + " = ? "
                + " AND " + ProductTagRelationTable.COLUMN_IS_ACTIVE + " = ? ",
                new String[]{String.valueOf(localProductId), String.valueOf(DBUtils.booleanToInt(true))}, null, null, null);
        return getProductTagRelationsFromQueriedCursor(cursor);
    }

    public ProductTagRelation getRelation(int remoteProductId, int remoteTagId) {
        Cursor cursor = database.query(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, allColumns,
                ProductTagRelationTable.COLUMN_PRODUCT_ID + " = ? "
                        + " AND " + ProductTagRelationTable.COLUMN_TAG_ID + " = ? ",
                new String[]{String.valueOf(remoteProductId), String.valueOf(remoteTagId)}, null, null, null);
        List<ProductTagRelation> queryResult = getProductTagRelationsFromQueriedCursor(cursor);
        ProductTagRelation result;
        if (queryResult.isEmpty()) {
            result = null;
        } else {
            result = queryResult.get(0);
        }
        return result;
    }

    public ProductTagRelation createRelation(Product product, Tag tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_TAG_ID, tag.getId());
        contentValues.put(ProductTagRelationTable.COLUMN_TAG_LOCAL_ID, tag.getLocalId());
        contentValues.put(ProductTagRelationTable.COLUMN_PRODUCT_ID, product.getId());
        contentValues.put(ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID, product.getLocalId());
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE, DBUtils.booleanToInt(true));
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP, DBUtils.getCurrentTimestamp());
        return createRelationInternal(contentValues);
    }

    public ProductTagRelation createRelationFromRemote(ProductTagRelation translatedRemote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_TAG_ID, translatedRemote.getTagId());
        contentValues.put(ProductTagRelationTable.COLUMN_TAG_LOCAL_ID, translatedRemote.getTagLocalId());
        contentValues.put(ProductTagRelationTable.COLUMN_PRODUCT_ID, translatedRemote.getProductId());
        contentValues.put(ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID, translatedRemote.getTagLocalId());
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE, DBUtils.booleanToInt(translatedRemote.getActive()));
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP, translatedRemote.getRelationUpdateTimestamp());
        return createRelationInternal(contentValues);
    }

    public void deleteRelation(long localId) {
        database.delete(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION,
                ProductTagRelationTable.COLUMN_LOCAL_ID + " = " + localId, null);
        Log.d(TAG, "Deleted relation id: " + localId);
    }

    public void updateRemoteTagId(long localId, int updatedRemoteTagId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_TAG_ID, updatedRemoteTagId);
        updateRelationInternal(localId, contentValues);
    }

    public void updateRemoteProductId(long localId, int updatedRemoteProductId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_PRODUCT_ID, updatedRemoteProductId);
        updateRelationInternal(localId, contentValues);
    }

    public void updateRelationActivity(long localId, boolean isActive) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE, DBUtils.booleanToInt(isActive));
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP, DBUtils.getCurrentTimestamp());
        updateRelationInternal(localId, contentValues);
    }

    public void updateRelationActivityFromRemote(ProductTagRelation updatedRelation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE, DBUtils.booleanToInt(updatedRelation.getActive()));
        contentValues.put(ProductTagRelationTable.COLUMN_IS_ACTIVE_UPDATE_TIMESTAMP, updatedRelation.getRelationUpdateTimestamp());
        updateRelationInternal(updatedRelation.getLocalId(), contentValues);
    }

    private ProductTagRelation createRelationInternal(ContentValues contentValues) {
        long insertId = database.insert(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, null,
                contentValues);
        ProductTagRelation created = getRelation(insertId);
        Log.d(TAG, "Created relation: " + created);
        return created;
    }

    private void updateRelationInternal(long localId, ContentValues contentValues) {
        database.update(ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION, contentValues,
                ProductTagRelationTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)});
    }

    @NonNull
    private List<ProductTagRelation> getProductTagRelationsFromQueriedCursor(Cursor cursor) {
        List<ProductTagRelation> relations = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ProductTagRelation relation = cursorToRelation(cursor);
            relations.add(relation);
            cursor.moveToNext();
        }
        cursor.close();
        return relations;
    }

    private ProductTagRelation cursorToRelation(Cursor cursor) {
        ProductTagRelation relation = new ProductTagRelation();
        relation.setLocalId(cursor.getLong(0));
        relation.setProductId(cursor.getInt(1));
        relation.setProductLocalId(cursor.getLong(2));
        relation.setTagId(cursor.getInt(3));
        relation.setTagLocalId(cursor.getLong(4));
        relation.setActive(DBUtils.intToBoolean(cursor.getInt(5)));
        relation.setRelationUpdateTimestamp(cursor.getLong(6));
        return relation;
    }

}
