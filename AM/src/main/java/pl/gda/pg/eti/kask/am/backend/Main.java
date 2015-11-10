package pl.gda.pg.eti.kask.am.backend;

import pl.gda.pg.eti.kask.am.backend.model.ProductEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by Kuba on 2015-11-09.
 */
public class Main {

    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-backend");
        EntityManager em = factory.createEntityManager();
        // read the existing entries and write to console
        Query q = em.createQuery("select t from ProductEntity t");
        List<ProductEntity> prodList = q.getResultList();
        for (ProductEntity prod : prodList) {
            System.out.println(prod);
        }
        System.out.println("Size: " + prodList.size());

        // create new todo
        em.getTransaction().begin();
        ProductEntity todo = new ProductEntity();
        todo.setOwnerGoogleId("123");
        todo.setQuantity(0);
        em.persist(todo);
        em.getTransaction().commit();

        em.close();
    }
}
