package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by JesseRawlings on 12/21/17.
 */

public class ItemCursorAdapter extends CursorAdapter {

    private Context globalContext;
    private Cursor globalCursor;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ItemCursorAdapter.class.getSimpleName();

    //constructor
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        globalContext = context;
        globalCursor = cursor;

        //get views to populate with data
        TextView nameTextview = (TextView) view.findViewById(R.id.name_textview);
        TextView priceTextview = (TextView) view.findViewById(R.id.price_textview);
        TextView quantityTextview = (TextView) view.findViewById(R.id.quantity_textview);

        //setup sold button to subtract 1 from the quantity
        Button soldButton = (Button) view.findViewById(R.id.sale_button);
        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentRow = (View) v.getParent();
                ListView listView = (ListView) parentRow.getParent();
                final int position = listView.getPositionForView(parentRow);
                globalCursor.moveToPosition(position);
                int quantityIndex = globalCursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
                int quantity = globalCursor.getInt(quantityIndex);
                if (quantity > 0) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                    Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, globalCursor.getInt(globalCursor.getColumnIndex(InventoryEntry._ID)));
                    globalContext.getContentResolver().update(uri, values, null, null);
                }
            }
        });

        //get data from cursor
        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));

        //get price from cursor
        String priceString = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));
        String price = "$" + priceString;

        //get quantity
        int quantityInt = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));

        String quantity = Integer.toString(quantityInt);

        //set data on views
        nameTextview.setText(name);
        priceTextview.setText(price);
        quantityTextview.setText(quantity);

    }

}
