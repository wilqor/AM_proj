package pl.gda.pg.eti.kask.am.backend.model;

import javax.persistence.*;

/**
 * Created by Kuba on 2016-01-24.
 */
@Entity
@Table(name = "product_tag")
@IdClass(ProductTagAssociationId.class)
public class ProductTagAssociation {
    @Id
    @Column(name = "product_id")
    private long productId;

    @Id
    @Column(name = "tag_id")
    private long tagId;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "relation_update_timestamp")
    private long relationUpdateTimestamp;

    @Column(name = "owner_google_id")
    private String ownerGoogleId;

    public String getOwnerGoogleId() {
        return ownerGoogleId;
    }

    public void setOwnerGoogleId(String ownerGoogleId) {
        this.ownerGoogleId = ownerGoogleId;
    }

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity productEntity;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "tag_id", referencedColumnName = "id")
    private TagEntity tagEntity;

    public ProductTagAssociation() {
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

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public long getRelationUpdateTimestamp() {
        return relationUpdateTimestamp;
    }

    public void setRelationUpdateTimestamp(long relationUpdateTimestamp) {
        this.relationUpdateTimestamp = relationUpdateTimestamp;
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public TagEntity getTagEntity() {
        return tagEntity;
    }

    public void setTagEntity(TagEntity tagEntity) {
        this.tagEntity = tagEntity;
    }

    public static ProductTagRelation toRelation(ProductTagAssociation association) {
        ProductTagRelation relation = new ProductTagRelation();
        relation.setActive(association.isActive);
        relation.setProductId((int) association.getProductId());
        relation.setTagId((int) association.getTagId());
        relation.setRelationUpdateTimestamp(association.getRelationUpdateTimestamp());
        return relation;
    }
}
