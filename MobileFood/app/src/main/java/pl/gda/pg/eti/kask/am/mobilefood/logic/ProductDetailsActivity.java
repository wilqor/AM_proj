package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.db.DBGetter;
import pl.gda.pg.eti.kask.am.mobilefood.db.MySQLiteHelper;
import pl.gda.pg.eti.kask.am.mobilefood.db.ProductTagRelationDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.db.ProductsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.db.TagsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.model.Product;
import pl.gda.pg.eti.kask.am.mobilefood.model.ProductPriority;
import pl.gda.pg.eti.kask.am.mobilefood.model.ProductTagRelation;
import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, ProductTagListActionHandler {
    private static final String TAG = "ProductDetailsActivity";

    private Product product;
    private ProductsDataSource productDataSource;
    private TagsDataSource tagsDataSource;
    private ProductTagRelationDataSource relationDataSource;
    private ProductTagListAdapter productTagListAdapter;
    private MySQLiteHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        getProductFromIntent();
        initializeView();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        findViewById(R.id.inc_btn).setOnClickListener(this);
        findViewById(R.id.dec_btn).setOnClickListener(this);
        findViewById(R.id.add_new_tag_btn).setOnClickListener(this);
        findViewById(R.id.add_existing_tag_btn).setOnClickListener(this);
    }

    private void getProductFromIntent() {
        Bundle bundle = getIntent().getExtras();
        product = (Product) bundle.get(Consts.PRODUCT_FOR_DETAILS);
        Log.d(TAG, "Received product: " + product + " from intent.");
    }

    private void initializeView() {
        TextView nameTextView = (TextView) findViewById(R.id.product_details_name);
        nameTextView.setText(product.getName());
        refreshQuantityLabel();
        initializePrioritySpinner();
    }

    private void initializePrioritySpinner() {
        Spinner prioritySpinner = (Spinner) findViewById(R.id.priority_spinner);
        ArrayAdapter<ProductPriority> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                ProductPriority.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int currentPosition = adapter.getPosition(product.getPriority());
        prioritySpinner.setAdapter(adapter);
        prioritySpinner.setSelection(currentPosition);
        prioritySpinner.setOnItemSelectedListener(this);
    }

    private void refreshQuantityLabel() {
        TextView quantityTextView = (TextView) findViewById(R.id.product_details_quantity);
        quantityTextView.setText("" + product.getQuantity());
    }

    @Override
    protected void onResume() {
        super.onResume();

        prepareDataSources();
        populateTagsListView();
    }

    private void populateTagsListView() {
        List<Tag> activeTags = tagsDataSource.getAllActiveTagsForProduct(product.getLocalId());
        Log.d(TAG, "Active tags for product: " + activeTags);
        ListView listView = (ListView) findViewById(R.id.product_tags_view);
        productTagListAdapter = new ProductTagListAdapter(this, R.layout.deletable_list_item, activeTags, this);
        listView.setAdapter(productTagListAdapter);
    }

    private void prepareDataSources() {
        DBGetter dbGetter = new DBGetter(this);
        helper = dbGetter.getHelper();
        SQLiteDatabase db = dbGetter.getDb();
        productDataSource = new ProductsDataSource(db);
        tagsDataSource = new TagsDataSource(db);
        relationDataSource = new ProductTagRelationDataSource(db);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (helper != null) {
            helper.close();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inc_btn:
                Log.d(TAG, "Received increase quantity click");
                showModifyQuantityDialog(true);
                break;
            case R.id.dec_btn:
                Log.d(TAG, "Received decrease quantity click");
                showModifyQuantityDialog(false);
                break;
            case R.id.add_new_tag_btn:
                Log.d(TAG, "Received add new tag click");
                showAddNewTagDialog();
                break;
            case R.id.add_existing_tag_btn:
                Log.d(TAG, "Received add existing tag click");
                showAddExistingDialog();
                break;
        }
    }

    private void showAddExistingDialog() {
        final List<Tag> availableTags = tagsDataSource.getAllInactiveTagsForProduct(product.getLocalId());
        Log.d(TAG, "Available tags for product: " + availableTags);
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setTitle("Select existing tag");
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        adapter.addAll(availableTags);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Tag selected = availableTags.get(which);
                Log.d(TAG, "Selected tag: " + selected);
                long productLocal = product.getLocalId();
                long tagLocalId = selected.getLocalId();
                ProductTagRelation relation = relationDataSource.getRelation(productLocal, tagLocalId);
                if (relation == null) {
                    relationDataSource.createRelation(product, selected);
                    Log.d(TAG, "Created relation with tag: " + selected);
                } else {
                    relationDataSource.updateRelationActivity(relation.getLocalId(), true);
                    Log.d(TAG, "Set relation with tag: " + selected + " to true");
                }
                productTagListAdapter.add(selected);
            }
        });
        builder.show();
    }


    private void showAddNewTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setTitle("Enter new tag name");
        final EditText input = new EditText(ProductDetailsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tagName = input.getText().toString().trim().toLowerCase();
                Log.d(TAG, "Entered tag name: " + tagName);
                boolean tagNameEmpty = tagName.isEmpty();
                boolean notASingleWord = tagName.contains(" ");
                boolean tagNameNotUnique = !tagsDataSource.isTagNameUnique(tagName);
                if (tagNameEmpty || notASingleWord || tagNameNotUnique) {
                    String errorMsg = "";
                    if (tagNameEmpty) {
                        errorMsg = "Tag name cannot be empty";
                    } else if (notASingleWord) {
                        errorMsg = "Tag name has to be a single word";
                    } else if (tagNameNotUnique) {
                        errorMsg = "Tag already exists";
                    }
                    Log.d(TAG, errorMsg);
                    showToast(errorMsg);
                    return;
                }
                Tag added = tagsDataSource.createTag(tagName);
                relationDataSource.createRelation(product, added);
                productTagListAdapter.add(added);
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

    @Override
    public void onProductTagDeleteClick(Tag tag) {
        Log.d(TAG, "Received product tag delete click for tag: " + tag);
        showDeleteProductTagDialog(tag);
    }

    private void showDeleteProductTagDialog(final Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        builder.setMessage("Do you want to delete association with " + tag.getName() + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long tagId = tag.getLocalId();
                long productId = product.getLocalId();
                ProductTagRelation relation = relationDataSource.getRelation(productId, tagId);
                if (relation != null) {
                    relationDataSource.updateRelationActivity(relation.getLocalId(), false);
                    Log.d(TAG, "Set association with: " + tag + " to inactive");
                }
                productTagListAdapter.remove(tag);
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

    private void showModifyQuantityDialog(boolean increase) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsActivity.this);
        String operationName = increase ? "INCREASE" : "DECREASE";
        final int operationSign = increase ? 1 : -1;
        builder.setTitle("Enter the amount to " + operationName + "  " + product.getName() + " quantity");
        final EditText input = new EditText(ProductDetailsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String increaseString = input.getText().toString();
                Log.d(TAG, "Entered product change quantity: " + increaseString);
                try {
                    int modification = operationSign * Integer.parseInt(increaseString);
                    updateProductWithModification(modification);
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

    private void updateProductWithModification(int quantityChange) {
        int newDeviceQuantity = product.getDeviceQuantity() + quantityChange;
        int previousQuantity = product.getQuantity();
        int newQuantity = previousQuantity + quantityChange;
        if (previousQuantity >= 0 && newQuantity < 0) {
            String errorMsg = "Product quantity cannot be set below negative";
            Log.d(TAG, errorMsg);;
            showToast(errorMsg);
            return;
        }
        product.setQuantity(newQuantity);
        product.setDeviceQuantity(newDeviceQuantity);
        productDataSource.updateProductQuantity(product.getLocalId(), product.getQuantity(), product.getDeviceQuantity());
        Log.d(TAG, "Updated product quantity for: " + product);
        refreshQuantityLabel();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ProductPriority priority = (ProductPriority) parent.getItemAtPosition(position);
        Log.d(TAG, "Priority: " + priority + " was chosen in spinner");
        if (priorityHasChanged(priority)) {
            productDataSource.updateProductPriority(product.getLocalId(), priority);
            product.setPriority(priority);
            Log.d(TAG, "Updated product priority in database");
        }
    }

    private boolean priorityHasChanged(ProductPriority priority) {
        return !priority.equals(product.getPriority());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
