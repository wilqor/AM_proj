package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.rest.ProductService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener,
        ProductActionHandler {

    private static final String TAG = "ProductActivity";

    private String userGoogleId;
    private String userGoogleToken;
    private String serverAddress;
    private ProductService productService;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        getUserParametersFromIntent();
        findViewById(R.id.button_add_product).setOnClickListener(this);
        findViewById(R.id.button_add_product).setEnabled(false);
        prepareProductService();
        populateListAdapter();
    }

    private void prepareProductService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverAddress)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        productService = retrofit.create(ProductService.class);
    }

    private void populateListAdapter() {
        Call<List<Product>> productCall = productService.getProducts(userGoogleId, userGoogleToken);
        productCall.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Response<List<Product>> response, Retrofit retrofit) {
                Log.d(TAG, "Response code for products is: " + response.code());
                if (response.isSuccess()) {
                    Log.d(TAG, "Success response");
                    List<Product> products = response.body();
                    Log.d(TAG, "Received: " + products.toString());
                    initializeListView(products);
                } else {
                    Log.d(TAG, "Error response");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Could not connect to service: " + t.getMessage());
            }
        });
    }

    private void initializeListView(List<Product> products) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        adapter = new ProductAdapter(ProductActivity.this, R.layout.product_item, products, this);
        listView.setAdapter(adapter);
        findViewById(R.id.button_add_product).setEnabled(true);
    }

    private void getUserParametersFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userGoogleId = bundle.getString(Consts.GOOGLE_ID_PARAMETER);
            userGoogleToken = bundle.getString(Consts.GOOGLE_ID_TOKEN);
            serverAddress = bundle.getString(Consts.SERVER_ADDRESS);
        }
        Log.d(TAG, "Received intent user id: " + userGoogleId);
        Log.d(TAG, "Received intent user token: " + userGoogleToken);
        Log.d(TAG, "Received server address: " + serverAddress);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_product:
                showAddProductDialog();
                break;
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        builder.setTitle("Enter new product name");
        final EditText input = new EditText(ProductActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String productName = input.getText().toString();
                Log.d(TAG, "Entered product name: " + productName);
                sendNewProductPost(productName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void sendNewProductPost(String productName) {
        if (isProductNameValid(productName)) {
            Product newProduct = new Product();
            newProduct.setName(productName);
            Call<Product> newProductCall = productService.addProduct(userGoogleId,
                    userGoogleToken, newProduct);
            newProductCall.enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Response<Product> response, Retrofit retrofit) {
                    Log.d(TAG, "Response code for posting product is: " + response.code());
                    if (response.isSuccess()) {
                        Log.d(TAG, "Success response");
                        Product product = response.body();
                        Log.d(TAG, "Received: " + product.toString());
                        updateAdapterWithNewProduct(product);
                    } else {
                        Log.d(TAG, "Error response");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(TAG, "Could not connect to service: " + t.getMessage());
                }
            });
        }
    }

    private void updateAdapterWithNewProduct(Product product) {
        adapter.add(product);
        Log.d(TAG, "Added new product to list adapter: " + product);
    }

    private boolean isProductNameValid(String productName) {
        return productName != null && !productName.isEmpty();
    }

    @Override
    public void onProductDeleteClick(Product product) {
        Log.d(TAG, "Received product delete click for product: " + product);
        showDeleteProductDialog(product);
    }

    private void showDeleteProductDialog(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        builder.setMessage("Do you want to delete " + product.getName() + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Deleted product: " + product.getName());
                sendProductDelete(product.getId());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void sendProductDelete(final int productId) {
        Call<String> deleteProductCall = productService.deleteProduct(userGoogleId,
                userGoogleToken, productId);
        deleteProductCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                Log.d(TAG, "Response code for deleting product is: " + response.code());
                if (response.isSuccess()) {
                    Log.d(TAG, "Success response");
                    removeProductFromAdapter(productId);
                } else {
                    Log.d(TAG, "Error response");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Could not connect to service: " + t.getMessage());
            }
        });
    }

    private void removeProductFromAdapter(int productId) {
        Product product = adapter.getById(productId);
        if (product != null) {
            adapter.remove(product);
        }
    }

    @Override
    public void onIncreaseQuantityClick(Product product) {
        Log.d(TAG, "Received inc click for: " + product);
        showModifyQuantityDialog(product, true);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showModifyQuantityDialog(final Product product, boolean increase) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        String operationName = increase ? "INCREASE" : "DECREASE";
        final int operationSign = increase ? 1 : -1;
        builder.setTitle("Enter the amount to " + operationName + "  " + product.getName() + " quantity");
        final EditText input = new EditText(ProductActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String increaseString = input.getText().toString();
                Log.d(TAG, "Entered product change quantity: " + increaseString);
                try {
                    int modification = operationSign * Integer.parseInt(increaseString);
                    prepareProductModification(modification, product);
                } catch (NumberFormatException e) {
                    String errorMsg = "Invalid product change quantity entered";
                    Log.d(TAG, errorMsg);
                    showToast(errorMsg);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void prepareProductModification(int quantityChange, Product product) {
        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            String errorMsg = "Product quantity cannot be negative";
            Log.d(TAG, errorMsg);
            showToast(errorMsg);
            return;
        }
        Product updatedProduct = new Product(product.getId(), product.getName(), newQuantity);
        sendProductUpdate(updatedProduct);
    }

    private void sendProductUpdate(final Product updatedProduct) {
        Call<String> updateProductCall = productService.updateProduct(userGoogleId,
                userGoogleToken, updatedProduct.getId(), updatedProduct);
        updateProductCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Response<String> response, Retrofit retrofit) {
                Log.d(TAG, "Response code for updating product is: " + response.code());
                if (response.isSuccess()) {
                    Log.d(TAG, "Success response");
                    updateProductInAdapter(updatedProduct);
                } else {
                    Log.d(TAG, "Error response");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Could not connect to service: " + t.getMessage());
            }
        });
    }

    private void updateProductInAdapter(Product updatedProduct) {
        Product product = adapter.getById(updatedProduct.getId());
        if (product != null) {
            product.setQuantity(updatedProduct.getQuantity());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDecreaseQuantityClick(Product product) {
        Log.d(TAG, "Received dec click for: " + product);
        showModifyQuantityDialog(product, false);
    }
}
