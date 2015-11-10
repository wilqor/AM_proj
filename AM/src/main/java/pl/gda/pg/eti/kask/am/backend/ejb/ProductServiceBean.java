package pl.gda.pg.eti.kask.am.backend.ejb;

import pl.gda.pg.eti.kask.am.backend.model.Product;
import pl.gda.pg.eti.kask.am.backend.model.ProductEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 2015-11-09.
 */
@Stateless
@LocalBean
public class ProductServiceBean implements Serializable {

    @PersistenceContext(unitName = "jpa-backend")
    EntityManager em;

    public ProductServiceBean() {
    }

    public List<ProductEntity> findProducts(String ownerGoogleId) {
        TypedQuery<ProductEntity> q = em.createQuery("SELECT p FROM ProductEntity p WHERE p.ownerGoogleId = :ownerGoogleId",
                ProductEntity.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        List<ProductEntity> products = q.getResultList();
        return products;
    }

    public ProductEntity createProduct(Product product, String ownerGoogleId) {
        ProductEntity entity = new ProductEntity(product, ownerGoogleId);
        em.persist(entity);
        return entity;
    }

    public ProductEntity findProduct(Integer id) {
        return em.find(ProductEntity.class, id);
    }

    public void removeProduct(ProductEntity entity) {
        em.remove(em.merge(entity));
    }

    public void updateProductQuantity(ProductEntity entity, Product product) {
        entity.setQuantity(product.getQuantity());
        em.merge(entity);
    }
}
