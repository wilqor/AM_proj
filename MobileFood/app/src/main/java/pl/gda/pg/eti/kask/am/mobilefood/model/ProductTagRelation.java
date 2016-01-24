package pl.gda.pg.eti.kask.am.mobilefood.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Kuba on 2016-01-20.
 */
public class ProductTagRelation {
    @Expose
    private long relationUpdateTimestamp;
    @Expose
    private boolean active;
    @Expose
    private Integer productId;
    @Expose
    private Integer tagId;
    private long localId;
    private long productLocalId;
    private long tagLocalId;

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

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public long getProductLocalId() {
        return productLocalId;
    }

    public void setProductLocalId(long productLocalId) {
        this.productLocalId = productLocalId;
    }

    public long getTagLocalId() {
        return tagLocalId;
    }

    public void setTagLocalId(long tagLocalId) {
        this.tagLocalId = tagLocalId;
    }

    @Override
    public String toString() {
        return "ProductTagRelation{" +
                "relationUpdateTimestamp=" + relationUpdateTimestamp +
                ", active=" + active +
                ", productId=" + productId +
                ", tagId=" + tagId +
                ", localId=" + localId +
                ", productLocalId=" + productLocalId +
                ", tagLocalId=" + tagLocalId +
                '}';
    }
}
