package pl.gda.pg.eti.kask.am.backend.model;

import org.eclipse.persistence.sessions.Project;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Kuba on 2016-01-24.
 */
@Entity
@NamedQuery(name = "TagEntity.findAll", query = "SELECT p FROM TagEntity p")
@Table(name = "tag")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_google_id")
    private String ownerGoogleId;

    @OneToMany(mappedBy = "tagEntity")
    private List<ProductTagAssociation> products;

    public TagEntity() {}

    public List<ProductTagAssociation> getProducts() {
        return products;
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

    public String getOwnerGoogleId() {
        return ownerGoogleId;
    }

    public void setOwnerGoogleId(String ownerGoogleId) {
        this.ownerGoogleId = ownerGoogleId;
    }

    public static Tag toTag(TagEntity tagEntity) {
        Tag tag = new Tag();
        tag.setId(tagEntity.getId());
        tag.setName(tagEntity.getName());
        return tag;
    }
}
