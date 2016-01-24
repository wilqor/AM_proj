package pl.gda.pg.eti.kask.am.backend.model;

import java.io.Serializable;

/**
 * Created by Kuba on 2016-01-24.
 */
public class ProductTagAssociationId implements Serializable {
    private long productId;
    private long tagId;

    public ProductTagAssociationId() {

    }

    public ProductTagAssociationId(long productId, long tagId) {
        this.productId = productId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductTagAssociationId that = (ProductTagAssociationId) o;

        if (productId != that.productId) return false;
        return tagId == that.tagId;
    }

    @Override
    public int hashCode() {
        int result = (int) (productId ^ (productId >>> 32));
        result = 31 * result + (int) (tagId ^ (tagId >>> 32));
        return result;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
