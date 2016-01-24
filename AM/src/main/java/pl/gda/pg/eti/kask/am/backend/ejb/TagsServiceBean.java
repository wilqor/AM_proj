package pl.gda.pg.eti.kask.am.backend.ejb;

import pl.gda.pg.eti.kask.am.backend.model.Tag;
import pl.gda.pg.eti.kask.am.backend.model.TagEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 2016-01-24.
 */
@Stateless
@LocalBean
public class TagsServiceBean implements Serializable {

    @PersistenceContext(unitName = "jpa-postgresql-backend")
    EntityManager em;

    public TagsServiceBean() {}

    public TagEntity findTagEntity(int id, String ownerGoogleId) {
        TypedQuery<TagEntity> q = em.createQuery("SELECT t FROM TagEntity t WHERE t.ownerGoogleId = :ownerGoogleId AND t.id = :id",
                TagEntity.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        q.setParameter("id", id);
        List<TagEntity> tagEntities = q.getResultList();
        return tagEntities.isEmpty() ? null : tagEntities.get(0);
    }

    public List<Tag> findTags(String ownerGoogleId) {
        TypedQuery<TagEntity> q = em.createQuery("SELECT t FROM TagEntity t WHERE t.ownerGoogleId = :ownerGoogleId",
                TagEntity.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        List<TagEntity> tagEntities = q.getResultList();
        List<Tag> tags = new ArrayList<>();
        for (TagEntity tagEntity : tagEntities) {
            tags.add(TagEntity.toTag(tagEntity));
        }
        return tags;
    }

    public List<Tag> updateTags(List<Tag> tags, String ownerGoogleId) {
        for (Tag tag : tags) {
            createOrUpdateTag(tag, ownerGoogleId);
        }
        return findTags(ownerGoogleId);
    }

    private void createOrUpdateTag(Tag tag, String ownerGoogleId) {
        TagEntity equivalent = getEquivalentTag(tag, ownerGoogleId);
        if (equivalent == null) {
            createTagEntity(tag, ownerGoogleId);
        }
    }

    private void createTagEntity(Tag tag, String ownerGoogleId) {
        TagEntity entity = new TagEntity();
        entity.setName(tag.getName());
        entity.setOwnerGoogleId(ownerGoogleId);
        em.persist(entity);
        em.flush();
    }

    private TagEntity getEquivalentTag(Tag tag, String ownerGooogleId) {
        if (!tag.notSynchronizedYet()) {
            TypedQuery<TagEntity> q = em.createQuery("SELECT t FROM TagEntity t WHERE t.ownerGoogleId = :ownerGoogleId AND t.name = :tagName",
                    TagEntity.class);
            q.setParameter("ownerGoogleId", ownerGooogleId);
            q.setParameter("tagName", tag.getName());
            List<TagEntity> foundTags = q.getResultList();
            if (!foundTags.isEmpty()) {
                return foundTags.get(0);
            }
        }
        return null;
    }
}
