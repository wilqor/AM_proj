package pl.gda.pg.eti.kask.am.mobilefood.logic;

import pl.gda.pg.eti.kask.am.mobilefood.model.Product;

/**
 * Created by Kuba on 2015-11-10.
 */
public interface ProductListActionHandler {
    void onProductDeleteClick(Product product);
    void onProductNameClick(Product product);
}
