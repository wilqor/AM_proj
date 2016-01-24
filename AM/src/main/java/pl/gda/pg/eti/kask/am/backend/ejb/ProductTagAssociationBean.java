package pl.gda.pg.eti.kask.am.backend.ejb;

import pl.gda.pg.eti.kask.am.backend.model.*;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Kuba on 2016-01-24.
 */
@Stateless
@LocalBean
public class ProductTagAssociationBean implements Serializable {

    @PersistenceContext(unitName = "jpa-postgresql-backend")
    EntityManager em;

    @EJB
    ProductServiceBean productServiceBean;

    @EJB
    TagsServiceBean tagsServiceBean;

    public ProductTagAssociationBean() {
    }

    public List<ProductTagRelation> findRelations(String ownerGoogleId) {
        TypedQuery<ProductTagAssociation> q = em.createQuery("SELECT p FROM ProductTagAssociation p WHERE p.ownerGoogleId = :ownerGoogleId",
                ProductTagAssociation.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        List<ProductTagAssociation> associations = q.getResultList();
        List<ProductTagRelation> relations = new ArrayList<>();
        for (ProductTagAssociation association : associations) {
            relations.add(ProductTagAssociation.toRelation(association));
        }
        return relations;
    }

    public List<ProductTagRelation> updateRelations(List<ProductTagRelation> relations, String ownerGoogleId) {
        for (ProductTagRelation relation : relations) {
            createOrUpdateRelation(relation, ownerGoogleId);
        }
        return findRelations(ownerGoogleId);
    }

    private void createOrUpdateRelation(ProductTagRelation relation, String ownerGoogleId) {
        ProductTagAssociation association = getEquivalentAssociation(relation, ownerGoogleId);
        if (association != null) {
            updateAssociation(relation, association);
        } else {
            createAssociation(relation, ownerGoogleId);
        }
    }

    private void updateAssociation(ProductTagRelation relation, ProductTagAssociation association) {
        if (relation.getRelationUpdateTimestamp() > association.getRelationUpdateTimestamp()) {
            association.setIsActive(relation.getActive());
            association.setRelationUpdateTimestamp(relation.getRelationUpdateTimestamp());
            em.merge(association);
            em.flush();
        }
    }

    private void createAssociation(ProductTagRelation relation, String ownerGoogleId) {
        ProductTagAssociation association = new ProductTagAssociation();
        ProductEntity product = productServiceBean.findProduct(relation.getProductId(), ownerGoogleId);
        TagEntity tag = tagsServiceBean.findTagEntity(relation.getTagId(), ownerGoogleId);
        if (product != null && tag != null) {
            association.setOwnerGoogleId(ownerGoogleId);
            association.setIsActive(relation.getActive());
            association.setRelationUpdateTimestamp(relation.getRelationUpdateTimestamp());
            association.setProductEntity(product);
            association.setProductId(product.getId());
            association.setTagEntity(tag);
            association.setTagId(tag.getId());
            // add to related tables
            product.getTags().add(association);
            tag.getProducts().add(association);
            em.persist(association);
            em.flush();
        }
    }

    private ProductTagAssociation getEquivalentAssociation(ProductTagRelation relation, String ownerGoogleId) {
        TypedQuery<ProductTagAssociation> q = em.createQuery("SELECT p FROM ProductTagAssociation p WHERE p.ownerGoogleId = :ownerGoogleId" +
                        " AND p.productId = :productId AND p.tagId = :tagId",
                ProductTagAssociation.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        q.setParameter("tagId", relation.getTagId());
        q.setParameter("productId", relation.getProductId());
        List<ProductTagAssociation> found = q.getResultList();
        if (!found.isEmpty()) {
            return found.get(0);
        } else {
            return null;
        }
    }

}
