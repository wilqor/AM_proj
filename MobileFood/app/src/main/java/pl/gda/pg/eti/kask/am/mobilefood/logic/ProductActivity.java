package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import pl.gda.pg.eti.kask.am.mobilefood.db.DBGetter;
import pl.gda.pg.eti.kask.am.mobilefood.db.ProductTagRelationDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.db.ProductsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.db.TagsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.model.ProductTagRelation;
import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;
import pl.gda.pg.eti.kask.am.mobilefood.rest.RemoteService;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener,
        ProductListActionHandler {
    private static final String TAG = "ProductActivity";

    private String userGoogleId;
    private String userGoogleToken;
    private String serverAddress;
    private String deviceId;
    private RemoteService remoteService;
    private ProductListAdapter adapter;
    private Set<String> idsToDelete;
    private Set<String> observedTagIds;
    private ProgressDialog progressDialog;
    private ProductsDataSource productDataSource;
    private ProductTagRelationDataSource relationsDataSource;
    private TagsDataSource tagsDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        getUserParametersFromIntent();
        setOnClickListenerForButtons();
        setAddProductAndSyncButtonsEnabled(false);
        prepareRemoteService();
        prepareProgressDialog();
    }

    private void setOnClickListenerForButtons() {
        findViewById(R.id.button_add_product).setOnClickListener(this);
        findViewById(R.id.button_sync).setOnClickListener(this);
        findViewById(R.id.button_configure_tags).setOnClickListener(this);
    }

    private void prepareDataSources() {
        SQLiteDatabase db = new DBGetter(this).getDb();
        productDataSource = new ProductsDataSource(db);
        relationsDataSource = new ProductTagRelationDataSource(db);
        tagsDataSource = new TagsDataSource(db);
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

        prepareDataSources();
        initializeIdsToDelete();
        initializeObservedTags();
        populateListAdapterAndSetViewEnabled();
        progressDialog.dismiss();
    }

    private void initializeIdsToDelete() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        idsToDelete = new HashSet<>(pref.getStringSet(Consts.SHARED_PREF_DELETE_ID_SET_KEY, new HashSet<String>()));
        Log.d(TAG, "Initialized with ids to delete set: " + idsToDelete);
    }

    private void initializeObservedTags() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        observedTagIds = new HashSet<>(pref.getStringSet(Consts.SHARED_PREF_OBSERVED_TAG_IDS_KEY, new HashSet<String>()));
        Log.d(TAG, "Initialized with observed tag ids: " + observedTagIds);
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

    private void prepareRemoteService() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = gsonBuilder.create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverAddress)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        remoteService = retrofit.create(RemoteService.class);
    }

    private void populateListAdapterAndSetViewEnabled() {
        populateListAdapter();
        setAddProductAndSyncButtonsEnabled(true);
    }

    private void populateListAdapter() {
        List<Long> tagIds = new ArrayList<>();
        for (String tagId : observedTagIds) {
            tagIds.add(Long.valueOf(tagId));
        }
        List<Product> productsFromDb = productDataSource.getAllProductsForTags(tagIds);
        Log.d(TAG, "Found products: " + productsFromDb);
        initializeListView(productsFromDb);
    }

    private void initializeListView(List<Product> products) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        adapter = new ProductListAdapter(ProductActivity.this, R.layout.deletable_list_item, products, this);
        listView.setAdapter(adapter);
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
            case R.id.button_configure_tags:
                Intent observedTagsIntent = new Intent(this, ObservedTagsActivity.class);
                Log.d(TAG, "Navigating to observed tags configuration");
                startActivity(observedTagsIntent);
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
                boolean productNameNotUnique = !productDataSource.isProductNameUnique(productName);
                if (productNameEmpty || productNameNotUnique) {
                    String errorMsg = productNameEmpty ? "Product name cannot be empty" : "Product name has to be unique";
                    Log.d(TAG, errorMsg);
                    showToast(errorMsg);
                    return;
                }
                Product newProduct = productDataSource.createProduct(productName);
                long newProductLocalId = newProduct.getLocalId();
                for (String localTagId : observedTagIds) {
                    long tagToAddId = Long.valueOf(localTagId);
                    Tag tagToAdd = tagsDataSource.getTag(tagToAddId);
                    relationsDataSource.createRelation(newProduct, tagToAdd);
                }
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

    private void updateAdapterWithNewProduct(Product product) {
        adapter.add(product);
        Log.d(TAG, "Added new product to list adapter: " + product);
    }

    @Override
    public void onProductDeleteClick(Product product) {
        Log.d(TAG, "Received product delete click for product: " + product);
        showDeleteProductDialog(product);
    }

    @Override
    public void onProductNameClick(Product product) {
        Log.d(TAG, "Received product name click for product: " + product);
        Intent productDetailsIntent = new Intent(this, ProductDetailsActivity.class);
        productDetailsIntent.putExtra(Consts.PRODUCT_FOR_DETAILS, product);
        startActivity(productDetailsIntent);
    }

    private void showDeleteProductDialog(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
        builder.setMessage("Do you want to delete " + product.getName() + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long productLocalId = product.getLocalId();
                productDataSource.deleteProduct(productLocalId);
                addProductIdToDelete(product);
                List<ProductTagRelation> relatedToProduct = relationsDataSource.getAllRelationsForProduct(productLocalId);
                for (ProductTagRelation relation : relatedToProduct) {
                    relationsDataSource.deleteRelation(relation.getLocalId());
                }
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

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void synchronizeRelationsState(List<ProductTagRelation> receivedRelations) {
        for (ProductTagRelation received : receivedRelations) {
            ProductTagRelation found = relationsDataSource.getRelation(received.getProductId(), received.getTagId());
            if (found != null) {
                found.setActive(received.getActive());
                found.setRelationUpdateTimestamp(received.getRelationUpdateTimestamp());
                relationsDataSource.updateRelationActivityFromRemote(found);
            } else {
                Product product = productDataSource.getProductForRemoteId(received.getProductId());
                Tag tag = tagsDataSource.getTagForRemoteId(received.getTagId());
                if (product != null && tag != null) {
                    received.setTagLocalId(tag.getLocalId());
                    received.setProductLocalId(product.getLocalId());
                    relationsDataSource.createRelationFromRemote(received);
                }
            }
        }
    }

    private void synchronizeTagsState(List<Tag> receivedTags, List<Tag> allTags) {
        List<Tag> localCopy = new ArrayList<>(allTags);
        List<Tag> receivedToBeCreated = new ArrayList<>();
        for (Tag received : receivedTags) {
            boolean includedInLocal = false;
            for (Tag local : localCopy) {
                if (received.getName().equals(local.getName())) {
                    includedInLocal = true;
                    break;
                }
            }
            if (!includedInLocal) {
                receivedToBeCreated.add(received);
            }
        }

        // create new
        for (Tag tag : receivedToBeCreated) {
            tagsDataSource.createTagFromRemote(tag);
        }
        receivedTags.removeAll(receivedToBeCreated);

        // now localCopy has occurring in both, same as receivedProducts
        for (Tag local : localCopy) {
            for (Tag received : receivedTags) {
                if (local.getName().equals(received.getName())) {
                    boolean shouldUpdate = !local.getId().equals(received.getId());
                    if (shouldUpdate) {
                        long localId = local.getLocalId();
                        int newId = received.getId();
                        tagsDataSource.updateTagRemoteId(localId, newId);
                        List<ProductTagRelation> relationsToUpdateTagId =
                                relationsDataSource.getAllRelationsForTag(localId);
                        for (ProductTagRelation relation : relationsToUpdateTagId) {
                            relationsDataSource.updateRemoteTagId(relation.getLocalId(), newId);
                        }
                    }
                }
            }
        }
    }

    public void synchronizeProductsState(List<Product> receivedProducts, List<Product> allProducts) {
        List<Product> localCopy = new ArrayList<>(allProducts);
        List<Product> localToBeDeleted = new ArrayList<>();
        for (Product local : localCopy) {
            boolean includedInReceived = false;
            for (Product received : receivedProducts) {
                if (local.getName().equals(received.getName())) {
                    includedInReceived = true;
                    break;
                }
            }
            if (!includedInReceived) {
                localToBeDeleted.add(local);
            }
        }
        // delete all from localToBeDeleted and adapter
        for (Product local : localToBeDeleted) {
            productDataSource.deleteProduct(local.getLocalId());
            List<ProductTagRelation> relationsToDelete = relationsDataSource.getAllRelationsForProduct(local.getLocalId());
            for (ProductTagRelation relation : relationsToDelete) {
                relationsDataSource.deleteRelation(relation.getLocalId());
            }
        }
        localCopy.removeAll(localToBeDeleted);

        // now localCopy has only those that should stay
        List<Product> receivedToBeCreated = new ArrayList<>();
        for (Product received : receivedProducts) {
            boolean includedInLocal = false;
            for (Product local : localCopy) {
                if (received.getName().equals(local.getName())) {
                    includedInLocal = true;
                    break;
                }
            }
            if (!includedInLocal) {
                receivedToBeCreated.add(received);
            }
        }

        // create
        for (Product received : receivedToBeCreated) {
            productDataSource.createProductFromRemote(received);
        }
        receivedProducts.removeAll(receivedToBeCreated);

        // now localCopy has occurring in both, same as receivedProducts
        for (Product local : localCopy) {
            for (Product received : receivedProducts) {
                if (local.getName().equals(received.getName())) {
                    boolean shouldUpdate = !local.getId().equals(received.getId()) ||
                            !(local.getQuantity() == received.getQuantity()) ||
                            !(local.getPriority() == received.getPriority()) ||
                            !(local.getPriorityUpdateTimestamp() == received.getPriorityUpdateTimestamp());
                    if (shouldUpdate) {
                        // update according product-tag relation remote id if id changed
                        if (local.getId() != received.getId()) {
                            long localId = local.getLocalId();
                            int newId = received.getId();
                            List<ProductTagRelation> relationsToUpdateProductId =
                                    relationsDataSource.getAllRelationsForProduct(localId);
                            for (ProductTagRelation relation : relationsToUpdateProductId) {
                                relationsDataSource.updateRemoteProductId(relation.getLocalId(), newId);
                            }
                        }
                        local.setId(received.getId());
                        local.setQuantity(received.getQuantity());
                        local.setPriority(received.getPriority());
                        local.setPriorityUpdateTimestamp(received.getPriorityUpdateTimestamp());
                        productDataSource.updateProductFromRemote(local);
                    }
                }
            }
        }
    }

    private class SynchronizeTask extends AsyncTask<Void, Void, Void> {
        private boolean networkError = false;
        private boolean syncError = false;
        List<Product> receivedProducts;
        List<Tag> receivedTags;
        List<ProductTagRelation> receivedRelations;

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
                putAndUpdateTags();
                syncDeleteOperations();
                putAndUpdateProducts();
                putAndUpdateRelations();
            } catch (IOException e) {
                networkError = true;
            } catch (SynchronizationException e) {
                syncError = true;
            }
            return null;
        }

        private void putAndUpdateRelations() throws IOException, SynchronizationException {
            final List<ProductTagRelation> allRelations = relationsDataSource.getAllRelations();
            Log.d(TAG, "All relations before sync: " + allRelations);
            Call<List<ProductTagRelation>> putRelationsCall = remoteService.putRelations(userGoogleId,
                    userGoogleToken, allRelations);
            Response<List<ProductTagRelation>> res = putRelationsCall.execute();
            if (res.isSuccess()) {
                receivedRelations = res.body();
                Log.d(TAG, "Successfully put relations list, received: " + receivedRelations);
                synchronizeRelationsState(receivedRelations);
            } else {
                Log.d(TAG, "Could not put relations list");
                throw new SynchronizationException("Operation failed, could not update");
            }
        }

        private void putAndUpdateTags() throws IOException, SynchronizationException {
            final List<Tag> allTags = tagsDataSource.getAllTags();
            Call<List<Tag>> putTagsCall = remoteService.putTags(userGoogleId,
                    userGoogleToken, allTags);
            Response<List<Tag>> res = putTagsCall.execute();
            if (res.isSuccess()) {
                receivedTags = res.body();
                Log.d(TAG, "Successfully put tags list, received: " + receivedTags);
                synchronizeTagsState(receivedTags, allTags);
            } else {
                Log.d(TAG, "Could not put tags list");
                throw new SynchronizationException("Operation failed, could not update");
            }
        }

        private void putAndUpdateProducts() throws IOException, SynchronizationException {
            final List<Product> allProducts = productDataSource.getAllProducts();
            Call<List<Product>> putProductsCall = remoteService.putProducts(userGoogleId,
                    userGoogleToken, deviceId, allProducts);
            Response<List<Product>> res = putProductsCall.execute();
            if (res.isSuccess()) {
                receivedProducts = res.body();
                Log.d(TAG, "Successfully put products list, received: " + receivedProducts);
                synchronizeProductsState(receivedProducts, allProducts);
            } else {
                Log.d(TAG, "Could not put products list");
                throw new SynchronizationException("Operation failed, could not update");
            }
        }

        private void syncDeleteOperations() throws IOException, SynchronizationException {
            for (String id : idsToDelete) {
                Call<String> deleteProductCall = remoteService.deleteProduct(userGoogleId,
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
            populateListAdapter();
            setAddProductAndSyncButtonsEnabled(true);
        }
    }

    private void setAddProductAndSyncButtonsEnabled(boolean enabled) {
        findViewById(R.id.button_add_product).setEnabled(enabled);
        findViewById(R.id.button_sync).setEnabled(enabled);
        findViewById(R.id.button_configure_tags).setEnabled(enabled);
    }
}
