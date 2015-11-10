package pl.gda.pg.eti.kask.am.backend.model;

/**
 * Created by Kuba on 2015-11-08.
 */
public class Product {
    private String name;
    private Integer id;
    private int quantity;

    public Product(){ }

    public Product(int id, String name, int quantity) {
        this.name = name;
        this.id = id;
        this.quantity = quantity;
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
}
