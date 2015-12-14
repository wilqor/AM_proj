package pl.gda.pg.eti.kask.am.mobilefood.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Kuba on 2015-11-09.
 */
public class Product {

    @Expose
    private String name;
    @Expose
    private Integer id;
    @Expose
    private int quantity;
    @Expose
    private int deviceQuantity;
    private long localId;

    public Product(){ }

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

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", quantity=" + quantity +
                ", deviceQuantity=" + deviceQuantity +
                ", localId=" + localId +
                '}';
    }

    public boolean notSynchronizedYet() {
        return id == 0;
    }
}
