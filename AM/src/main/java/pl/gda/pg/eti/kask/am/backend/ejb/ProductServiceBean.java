package pl.gda.pg.eti.kask.am.backend.ejb;

import pl.gda.pg.eti.kask.am.backend.model.Product;
import pl.gda.pg.eti.kask.am.backend.model.ProductEntity;
import pl.gda.pg.eti.kask.am.backend.model.ProductSubsetEntity;

import javax.annotation.CheckForNull;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext(unitName = "jpa-postgresql-backend")
    EntityManager em;

    public ProductServiceBean() {
    }

    public List<Product> findProducts(String ownerGoogleId, String deviceId) {
        TypedQuery<ProductEntity> q = em.createQuery("SELECT p FROM ProductEntity p WHERE p.ownerGoogleId = :ownerGoogleId",
                ProductEntity.class);
        q.setParameter("ownerGoogleId", ownerGoogleId);
        List<ProductEntity> productEntities = q.getResultList();
        List<Product> products = new ArrayList<>();
        for (ProductEntity entity : productEntities) {
            ProductSubsetEntity updatedSubset = findSubsetForDevice(deviceId, entity);
            int deviceQuantity = 0;
            if (updatedSubset != null) {
                deviceQuantity = updatedSubset.getQuantity();
            }
            products.add(ProductEntity.toProduct(entity, deviceQuantity));
        }
        return products;
    }

    public ProductEntity createProduct(Product product, String ownerGoogleId, String deviceId) {
        ProductEntity entity = new ProductEntity(product, ownerGoogleId);
        ProductSubsetEntity subsetEntity = new ProductSubsetEntity(deviceId, product.getDeviceQuantity());
        subsetEntity.setProduct(entity);
        entity.getSubsets().add(subsetEntity);
        entity.setQuantity(recalculateProductQuantity(entity));
        em.persist(entity);
        em.flush();
        return entity;
    }

    public ProductEntity findProduct(Integer id) {
        return em.find(ProductEntity.class, id);
    }

    public void removeProduct(ProductEntity entity) {
        em.remove(em.merge(entity));
    }

    public void updateProductQuantity(ProductEntity entity, Product product, String deviceId) {
        ProductSubsetEntity updatedSubset = findSubsetForDevice(deviceId, entity);
        if (updatedSubset == null) {
            updatedSubset = new ProductSubsetEntity(deviceId);
            updatedSubset.setProduct(entity);
            entity.getSubsets().add(updatedSubset);
        }
        updatedSubset.setQuantity(product.getDeviceQuantity());
        entity.setQuantity(recalculateProductQuantity(entity));
        em.merge(entity);
        em.flush();
    }

    public Product createOrUpdateProduct(Product product, String ownerGoogleId, String deviceId) {
        ProductEntity equivalent = getEquivalentProduct(product, ownerGoogleId);
        if (equivalent != null) {
            updateProductQuantity(equivalent, product, deviceId);
            return ProductEntity.toProduct(equivalent, product.getDeviceQuantity());
        } else if (product.notSynchronizedYet()) {
            equivalent = createProduct(product, ownerGoogleId, deviceId);
            return ProductEntity.toProduct(equivalent, product.getDeviceQuantity());
        }
        return null;
    }

    private
    @CheckForNull
    ProductEntity getEquivalentProduct(Product product, String ownerGoogleId) {
        if (!product.notSynchronizedYet()) {
            TypedQuery<ProductEntity> q = em.createQuery("SELECT p FROM ProductEntity p WHERE p.ownerGoogleId = :ownerGoogleId AND p.name = :productName",
                    ProductEntity.class);
            q.setParameter("ownerGoogleId", ownerGoogleId);
            q.setParameter("productName", product.getName());
            List<ProductEntity> foundProducts = q.getResultList();
            if (!foundProducts.isEmpty()) {
                return foundProducts.get(0);
            }
        }
        return null;
    }

    private
    @CheckForNull
    ProductSubsetEntity findSubsetForDevice(String deviceId, ProductEntity productEntity) {
        for (ProductSubsetEntity subset : productEntity.getSubsets()) {
            if (subset.getDeviceId().equals(deviceId)) {
                return subset;
            }
        }
        return null;
    }

    private int recalculateProductQuantity(ProductEntity productEntity) {
        int quantity = 0;
        for (ProductSubsetEntity subset : productEntity.getSubsets()) {
            quantity += subset.getQuantity();
        }
        return quantity;
    }

    public List<Product> updateProducts(List<Product> products, String ownerGoogleId, String deviceId) {
        for (Product product : products) {
            createOrUpdateProduct(product, ownerGoogleId, deviceId);
        }
        return findProducts(ownerGoogleId, deviceId);
    }
}
