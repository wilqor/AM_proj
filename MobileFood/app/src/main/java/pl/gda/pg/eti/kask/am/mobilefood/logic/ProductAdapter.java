package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.model.Product;

/**
 * Created by Kuba on 2015-11-10.
 */
public class ProductAdapter extends ArrayAdapter<Product> {

    private final List<Product> products;
    private final ProductActionHandler handler;

    public ProductAdapter(Context context, int textViewResourceId, List<Product> products,
                          ProductActionHandler handler) {
        super(context, textViewResourceId, products);
        this.products = products;
        this.handler = handler;
    }

    public Product get(long localId) {
        for (Product prod : products) {
            if (prod.getLocalId() == localId)
                return prod;
        }
        return null;
    }

    public List<Product> getProducts() {
        return products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.product_item, null);
        }

        final Product prod = products.get(position);
        if (prod != null) {

            TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
            listItemText.setText("[" + prod.getQuantity() + "] " + prod.getName());

            ImageButton decBtn = (ImageButton) view.findViewById(R.id.dec_btn);
            ImageButton incBtn = (ImageButton) view.findViewById(R.id.inc_btn);
            ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_btn);

            decBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onDecreaseQuantityClick(prod);
                }
            });

            incBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onIncreaseQuantityClick(prod);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onProductDeleteClick(prod);
                }
            });
        }

        return view;
    }
}
