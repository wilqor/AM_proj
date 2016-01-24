package pl.gda.pg.eti.kask.am.mobilefood.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by Kuba on 2015-11-09.
 */
public class Product implements Serializable {
    @Expose
    private String name;
    @Expose
    private Integer id;
    @Expose
    private int quantity;
    @Expose
    private int deviceQuantity;
    @Expose
    private ProductPriority priority;
    @Expose
    private long priorityUpdateTimestamp;
    private long localId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDeviceQuantity() {
        return deviceQuantity;
    }

    public void setDeviceQuantity(int deviceQuantity) {
        this.deviceQuantity = deviceQuantity;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public long getPriorityUpdateTimestamp() {
        return priorityUpdateTimestamp;
    }

    public void setPriorityUpdateTimestamp(long priorityUpdateTimestamp) {
        this.priorityUpdateTimestamp = priorityUpdateTimestamp;
    }

    public ProductPriority getPriority() {
        return priority;
    }

    public void setPriority(ProductPriority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", quantity=" + quantity +
                ", deviceQuantity=" + deviceQuantity +
                ", priority=" + priority +
                ", priorityUpdateTimestamp=" + priorityUpdateTimestamp +
                ", localId=" + localId +
                '}';
    }

    public boolean notSynchronizedYet() {
        return id == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return name.equals(product.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
