package pl.gda.pg.eti.kask.am.backend.model;

/**
 * Created by Kuba on 2016-01-24.
 */
public class Tag {
    private Integer id;
    private String name;

    public Tag() {
    }

    public Tag(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean notSynchronizedYet() { return id == 0;}
}
