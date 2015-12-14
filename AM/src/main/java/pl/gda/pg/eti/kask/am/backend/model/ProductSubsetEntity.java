package pl.gda.pg.eti.kask.am.backend.model;

import javax.persistence.*;

/**
 * Created by Kuba on 2015-12-13.
 */

@Entity
@Table(name = "product_subset")
public class ProductSubsetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    public ProductSubsetEntity() {
    }

    public ProductSubsetEntity(String deviceId) {
        this.deviceId = deviceId;
    }

    public ProductSubsetEntity(String deviceId, Integer quantity) {
        this.deviceId = deviceId;
        this.quantity = quantity;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }
}
