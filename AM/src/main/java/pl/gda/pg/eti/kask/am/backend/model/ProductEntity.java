package pl.gda.pg.eti.kask.am.backend.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 2015-11-09.
 */

@Entity
@NamedQuery(name = "ProductEntity.findAll", query = "SELECT p FROM ProductEntity p")
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_google_id")
    private String ownerGoogleId;

    @Enumerated(EnumType.STRING)
    private ProductPriority priority;

    @Column(name = "priority_update_timestamp")
    private long priorityUpdateTimestamp;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductSubsetEntity> subsets = new ArrayList<>();

    @OneToMany(mappedBy = "productEntity", cascade = CascadeType.ALL)
    private List<ProductTagAssociation> tags;

    public List<ProductTagAssociation> getTags() {
        return tags;
    }

    public ProductEntity() {}

    public ProductEntity(Product product, String ownerGoogleId) {
        this.name = product.getName();
        if (product.getPriority() != null) {
            this.priority = product.getPriority();
        } else {
            this.priority = ProductPriority.MEDIUM;
        }
        this.priorityUpdateTimestamp = product.getPriorityUpdateTimestamp();
        this.ownerGoogleId = ownerGoogleId;
        this.quantity = 0;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getOwnerGoogleId() {
        return ownerGoogleId;
    }

    public void setOwnerGoogleId(String ownerGoogleId) {
        this.ownerGoogleId = ownerGoogleId;
    }

    public Integer getId() {
        return id;
    }

    public void setPriority(ProductPriority priority) {
        this.priority = priority;
    }

    public void setPriorityUpdateTimestamp(long priorityUpdateTimestamp) {
        this.priorityUpdateTimestamp = priorityUpdateTimestamp;
    }

    public ProductPriority getPriority() {
        return priority;
    }

    public long getPriorityUpdateTimestamp() {
        return priorityUpdateTimestamp;
    }

    public List<ProductSubsetEntity> getSubsets() {
        return subsets;
    }

    public static Product toProduct(ProductEntity entity) {
        return new Product(entity.id, entity.name, entity.quantity, entity.priority, entity.priorityUpdateTimestamp);
    }

    public static Product toProduct(ProductEntity entity, int deviceQuantity) {
        return new Product(entity.id, entity.name, entity.quantity, entity.priority, entity.priorityUpdateTimestamp, deviceQuantity);
    }

    public static List<Product> toProducts(List<ProductEntity> entities) {
        List<Product> result = new ArrayList<>();
        for (ProductEntity entity : entities) {
            result.add(toProduct(entity));
        }
        return result;
    }

}
