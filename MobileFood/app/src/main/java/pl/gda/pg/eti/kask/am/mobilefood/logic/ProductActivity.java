package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.db.ProductsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.rest.ProductService;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener,
        ProductActionHandler {

    private static final String TAG = "ProductActivity";

    private String userGoogleId;
    private String userGoogleToken;
    private String serverAddress;
    private String deviceId;
    private ProductService productService;
    private ProductAdapter adapter;
    private Set<String> idsToDelete;
    private ProductsDataSource dataSource;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        dataSource = new ProductsDataSource(this);
        dataSource.open();
        getUserParametersFromIntent();
        findViewById(R.id.button_add_product).setOnClickListener(this);
        findViewById(R.id.button_sync).setOnClickListener(this);
        setAddProductAndSyncButtonsEnabled(false);
        prepareProductService();
        populateListAdapter();
        prepareProgressDialog();
    }

    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Synchronizing");
        progressDialog.setMessage("Synchronization in progress...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        dataSource.open();
        initializeIdsToDelete();
        progressDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();

        dataSource.close();
    }

    private void initializeIdsToDelete() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        idsToDelete = new HashSet<>(pref.getStringSet(Consts.SHARED_PREF_DELETE_ID_SET_KEY, new HashSet<String>()));
        Log.d(TAG, "Initialized with ids to delete set: " + idsToDelete);
    }

    private void saveIdsToDelete() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(Consts.SHARED_PREF_DELETE_ID_SET_KEY, idsToDelete);
        editor.commit();
        Log.d(TAG, "Saved ids to delete set: " + idsToDelete);
    }

    private void addProductIdToDelete(Product product) {
        if (!product.notSynchronizedYet()) {
            idsToDelete.add(String.valueOf(product.getId()));
            saveIdsToDelete();
        }
    }

    private void clearIdsToDelete() {
        idsToDelete.clear();
        saveIdsToDelete();
    }

    private void prepareProductService() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = gsonBuilder.create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverAddress)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        productService = retrofit.create(ProductService.class);
    }

    private void populateListAdapter() {
        List<Product> productsFromDb = dataSource.getAllProducts();
        initializeListView(productsFromDb);
    }

    private void initializeListView(List<Product> products) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        adapter = new ProductAdapter(ProductActivity.this, R.layout.product_item, products, this);
        listView.setAdapter(adapter);
        setAddProductAndSyncButtonsEnabled(true);
    }

    private void getUserParametersFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userGoogleId = bundle.getString(Consts.GOOGLE_ID_PARAMETER);
            userGoogleToken = bundle.getString(Consts.GOOGLE_ID_TOKEN);
            serverAddress = bundle.getString(Consts.SERVER_ADDRESS);
            deviceId = bundle.getString(Consts.DEVICE_ID);
        }
        Log.d(TAG, "Received intent user id: " + userGoogleId);
        Log.d(TAG, "Received intent user token: " + userGoogleToken);
        Log.d(TAG, "Received server address: " + serverAddress);
        Log.d(TAG, "Received device id: " + deviceId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_product:
                showAddProductDialog();
                break;
            case R.id.button_sync:
                new SynchronizeTask().execute();
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
                String productName = input.getText().toString().trim();
                Log.d(TAG, "Entered product name: " + productName);
                boolean productNameEmpty = productName.isEmpty();
                boolean productNameNotUnique = !isNewProductNameUnique(productName);
                if (productNameEmpty || productNameNotUnique) {
                    String errorMsg = productNameEmpty ? "Product name cannot be empty" : "Product name has to be unique";
                    Log.d(TAG, errorMsg);
                    showToast(errorMsg);
                    return;
                }
                Product newProduct = dataSource.createProduct(productName);
                updateAdapterWithNewProduct(newProduct);
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

    private boolean isNewProductNameUnique(String productName) {
        List<Product> products = adapter.getProducts();
        for (Product p : products) {
            if (p.getName().equals(productName))
                return false;
        }
        return true;
    }

    private void updateAdapterWithNewProduct(Product product) {
        adapter.add(product);
        Log.d(TAG, "Added new product to list adapter: " + product);
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
                dataSource.deleteProduct(product.getLocalId());
                addProductIdToDelete(product);
                removeProductFromAdapter(product.getLocalId());
                Log.d(TAG, "Deleted product: " + product.getName());
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

    private void removeProductFromAdapter(long localId) {
        Product product = adapter.get(localId);
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
                    updateProductWithModification(modification, product);
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

    private void updateProductWithModification(int quantityChange, Product product) {
        int newDeviceQuantity = product.getDeviceQuantity() + quantityChange;
        int previousQuantity = product.getQuantity();
        int newQuantity = previousQuantity + quantityChange;
        if (previousQuantity >= 0 && newQuantity < 0) {
            String errorMsg = "Product quantity cannot be set below negative";
            Log.d(TAG, errorMsg);
            showToast(errorMsg);
            return;
        }
        product.setQuantity(newQuantity);
        product.setDeviceQuantity(newDeviceQuantity);
        adapter.notifyDataSetChanged();
        dataSource.updateProduct(product.getLocalId(), product.getQuantity(), product.getDeviceQuantity());
        Log.d(TAG, "Prepared product for update: " + product);
    }

    @Override
    public void onDecreaseQuantityClick(Product product) {
        Log.d(TAG, "Received dec click for: " + product);
        showModifyQuantityDialog(product, false);
    }

    public void synchronizeProductsState(List<Product> receivedProducts) {
        List<Product> localCopy = new ArrayList<>(adapter.getProducts());

        List<Product> localToBeDeleted = new ArrayList<>();
        for (Product local : localCopy) {
            boolean includedInReceived = false;
            for (Product received : receivedProducts) {
                if (local.getName().equals(received.getName())) {
                    includedInReceived = true;
                }
            }
            if (!includedInReceived) {
                localToBeDeleted.add(local);
            }
        }
        // delete all from localToBeDeleted and adapter
        for (Product local : localToBeDeleted) {
            dataSource.deleteProduct(local.getLocalId());
        }
        localCopy.removeAll(localToBeDeleted);
        adapter.getProducts().removeAll(localToBeDeleted);
        // now localCopy has only those that should stay

        List<Product> receivedToBeCreated = new ArrayList<>();
        for (Product received : receivedProducts) {
            boolean includedInLocal = false;
            for (Product local : localCopy) {
                if (received.getName().equals(local.getName())) {
                    includedInLocal = true;
                }
            }
            if (!includedInLocal) {
                receivedToBeCreated.add(received);
            }
        }
        // create and add to collection
        List<Product> createdFromReceived = new ArrayList<>();
        for (Product received : receivedToBeCreated) {
            Product created = dataSource.createProductFromRemote(received.getName(), received.getId(),
                    received.getQuantity());
            createdFromReceived.add(created);
        }
        receivedProducts.removeAll(receivedToBeCreated);
        adapter.getProducts().addAll(createdFromReceived);

        // now localCopy has occurring in both, same as receivedProducts
        for (Product local : localCopy) {
            for (Product received : receivedProducts) {
                if (local.getName().equals(received.getName())) {
                    boolean shouldUpdate = !local.getId().equals(received.getId()) || !(local.getQuantity() == received.getQuantity());
                    if (shouldUpdate) {
                        local.setId(received.getId());
                        local.setQuantity(received.getQuantity());
                        dataSource.updateProductFromRemote(local.getLocalId(), local.getId(), local.getQuantity());
                    }
                }
            }
        }
    }

    private class SynchronizeTask extends AsyncTask<Void, Void, Void> {

        private boolean networkError = false;
        private boolean syncError = false;
        List<Product> receivedProducts;

        private class SynchronizationException extends Exception {
            public SynchronizationException(String message) {
                super(message);
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            setAddProductAndSyncButtonsEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                syncDeleteOperations();
                putAndUpdateCollection();
            } catch (IOException e) {
                networkError = true;
            } catch (SynchronizationException e) {
                syncError = true;
            }
            return null;
        }

        private void putAndUpdateCollection() throws IOException, SynchronizationException {
            Call<List<Product>> putProductsCall = productService.putProducts(userGoogleId,
                    userGoogleToken, deviceId, adapter.getProducts());
            Response<List<Product>> res = putProductsCall.execute();
            if (res.isSuccess()) {
                receivedProducts = res.body();
                Log.d(TAG, "Successfully put products list, received: " + receivedProducts);
                synchronizeProductsState(receivedProducts);
            } else {
                Log.d(TAG, "Could not put products list");
                throw new SynchronizationException("Operation failed, could not update");
            }
        }

        private void syncDeleteOperations() throws IOException, SynchronizationException {
            for (String id : idsToDelete) {
                Call<String> deleteProductCall = productService.deleteProduct(userGoogleId,
                        userGoogleToken, deviceId, Integer.valueOf(id));
                Response<String> res = deleteProductCall.execute();
                if (res.isSuccess()) {
                    Log.d(TAG, "Successfully deleted product with id: " + id);
                } else {
                    Log.d(TAG, "Could not delete product with id: " + id);
                }
                checkResponseAuthorization(res);
            }
            clearIdsToDelete();
        }

        @Override
        protected void onPostExecute(Void v) {
            if (networkError) {
                showNetworkError();
            } else if (syncError) {
                showSynchronizationError();
            }
            restoreView();
        }

        private void checkResponseAuthorization(Response<?> res) throws SynchronizationException {
            if (res.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new SynchronizationException("Unauthorized access, try to sign in again");
            }
        }

        private void showSynchronizationError() {
            String errorMessage = "Could not synchronize due to server-side error";
            Log.d(TAG, errorMessage);
            showToast(errorMessage);
        }

        private void showNetworkError() {
            String errorMessage = "Network connection error, cannot synchronize";
            Log.d(TAG, errorMessage);
            showToast(errorMessage);
        }

        private void restoreView() {
            ProductActivity.this.adapter.notifyDataSetChanged();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            setAddProductAndSyncButtonsEnabled(true);
        }
    }

    private void setAddProductAndSyncButtonsEnabled(boolean enabled) {
        findViewById(R.id.button_add_product).setEnabled(enabled);
        findViewById(R.id.button_sync).setEnabled(enabled);
    }
}
