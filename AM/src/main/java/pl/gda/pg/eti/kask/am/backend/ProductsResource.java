package pl.gda.pg.eti.kask.am.backend;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import pl.gda.pg.eti.kask.am.backend.ejb.ProductServiceBean;
import pl.gda.pg.eti.kask.am.backend.model.Product;
import pl.gda.pg.eti.kask.am.backend.model.ProductEntity;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * Created by Kuba on 2015-11-08.
 */
@Path("/products")
public class ProductsResource {

    @EJB
    private ProductServiceBean productBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                @HeaderParam("deviceId") String deviceId) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<Product> resultProducts = productBean.findProducts(ownerGoogleId, deviceId);
        return Response.ok(productsToGenericList(resultProducts)).build();
    }

    private GenericEntity<List<Product>> productsToGenericList(final List<Product> resultProducts) {
        return new GenericEntity<List<Product>>(resultProducts) {
            };
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProduct(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                               @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                               @HeaderParam("deviceId") String deviceId,
                               Product product) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)
                || !product.notSynchronizedYet() || product.getQuantity() != 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Product entity = productBean.createOrUpdateProduct(product, ownerGoogleId, deviceId);
        if (entity == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(entity).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                   @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                   @HeaderParam("deviceId") String deviceId,
                                   @PathParam("id") Integer id) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken) || id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        ProductEntity entity = productBean.findProduct(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean isOwnerOfProduct = ownerGoogleId.equals(entity.getOwnerGoogleId());
        if (!isOwnerOfProduct) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Product product = ProductEntity.toProduct(entity);
        return Response.ok(product).build();
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    public Response deleteProduct(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                  @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                  @HeaderParam("deviceId") String deviceId,
                                  @PathParam("id") Integer id) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken) || id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        ProductEntity entity = productBean.findProduct(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean isOwnerOfProduct = ownerGoogleId.equals(entity.getOwnerGoogleId());
        if (!isOwnerOfProduct) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        productBean.removeProduct(entity);
        return Response.ok().build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    public Response updateProductQuantity(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                          @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                          @HeaderParam("deviceId") String deviceId,
                                          @PathParam("id") Integer id, Product product) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)
                || id == null || product == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Product entity = productBean.createOrUpdateProduct(product, ownerGoogleId, deviceId);
        if (entity == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(entity).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProductList(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                      @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                      @HeaderParam("deviceId") String deviceId,
                                      List<Product> products) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)
                || products == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<Product> responseProducts = productBean.updateProducts(products, ownerGoogleId, deviceId);
        return Response.ok(productsToGenericList(responseProducts)).build();
    }

    private boolean verified(String ownerGoogleId, String ownerGoogleToken) {
        return new AccountVerifier(ownerGoogleId, ownerGoogleToken).verify();
    }
}
