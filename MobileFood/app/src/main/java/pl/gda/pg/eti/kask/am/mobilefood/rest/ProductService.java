package pl.gda.pg.eti.kask.am.mobilefood.rest;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by Kuba on 2015-11-09.
 */
public interface ProductService {

    @GET("/Backend/api/products")
    Call<List<Product>> getProducts(@Header("ownerGoogleId") String googleId,
                                    @Header("ownerGoogleToken") String ownerGoogleToken);

    @GET("/Backend/api/products/{id}")
    Call<Product> getProduct(@Header("ownerGoogleId") String googleId,
                             @Header("ownerGoogleToken") String ownerGoogleToken,
                             @Path("id") Integer productId);

    @POST("/Backend/api/products")
    Call<Product> addProduct(@Header("ownerGoogleId") String googleId,
                             @Header("ownerGoogleToken") String ownerGoogleToken,
                             @Body Product newProduct);

    @DELETE("/Backend/api/products/{id}")
    Call<String> deleteProduct(@Header("ownerGoogleId") String googleId,
                               @Header("ownerGoogleToken") String ownerGoogleToken,
                               @Path("id") Integer productId);

    @PUT("/Backend/api/products/{id}")
    Call<String> updateProduct(@Header("ownerGoogleId") String googleId,
                               @Header("ownerGoogleToken") String ownerGoogleToken,
                               @Path("id") Integer productId,
                               @Body Product updatedProduct);
}
