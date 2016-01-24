package pl.gda.pg.eti.kask.am.backend.model;

/**
 * Created by Kuba on 2016-01-24.
 */
public class ProductTagRelation {
    private long relationUpdateTimestamp;
    private boolean active;
    private Integer productId;
    private Integer tagId;

    public ProductTagRelation() {}

    public long getRelationUpdateTimestamp() {
        return relationUpdateTimestamp;
    }

    public void setRelationUpdateTimestamp(long relationUpdateTimestamp) {
        this.relationUpdateTimestamp = relationUpdateTimestamp;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }
}
