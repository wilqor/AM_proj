package pl.gda.pg.eti.kask.am.backend.model;

/**
 * Created by Kuba on 2015-11-08.
 */
public class Product {
    private String name;
    private Integer id;
    private int quantity;
    private int deviceQuantity;
    private ProductPriority priority;
    private long priorityUpdateTimestamp;

    public Product(){ }

    public Product(int id, String name, int quantity, ProductPriority priority, long priorityUpdateTimestamp) {
        this(id, name, quantity, priority, priorityUpdateTimestamp, 0);
    }

    public Product(int id, String name, int quantity, ProductPriority priority, long priorityUpdateTimestamp, int deviceQuantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        if (priority != null) {
            this.priority = priority;
        } else {
            this.priority = ProductPriority.MEDIUM;
        }
        this.priorityUpdateTimestamp = priorityUpdateTimestamp;
        this.deviceQuantity = deviceQuantity;
    }

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

    public boolean notSynchronizedYet() {
        return id == 0;
    }

    public ProductPriority getPriority() {
        return priority;
    }

    public void setPriority(ProductPriority priority) {
        this.priority = priority;
    }

    public long getPriorityUpdateTimestamp() {
        return priorityUpdateTimestamp;
    }

    public void setPriorityUpdateTimestamp(long priorityUpdateTimestamp) {
        this.priorityUpdateTimestamp = priorityUpdateTimestamp;
    }
}
