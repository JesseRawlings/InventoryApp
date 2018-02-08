package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

;

/**
 * Created by JesseRawlings on 12/21/17.
 */

public class ItemDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ItemDetailActivity.class.getSimpleName();

    //current uri
    private Uri currentUri;
    private static final int EXISTING_INVENTORY_LOADER = 0;

    //views with the data
    private TextView nameTextview;
    private TextView priceTextview;
    private TextView quantityTextview;
    private Button minusButton;
    private Button plusButton;
    private Button deleteButton;
    private Button orderButton;
    private ImageView itemImageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail_view);

        //initialize all views
        nameTextview = (TextView) findViewById(R.id.detail_name);
        priceTextview = (TextView) findViewById(R.id.detail_price);
        quantityTextview = (TextView) findViewById(R.id.quantity_number);
        itemImageView = (ImageView) findViewById(R.id.detail_image);

        minusButton = (Button) findViewById(R.id.minus_button);
        plusButton = (Button) findViewById(R.id.plus_button);
        deleteButton = (Button) findViewById(R.id.delete_button);
        orderButton = (Button) findViewById(R.id.order_button);

        //subtract 1 when minus is clicked
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });

        //add 1 when item is clicked
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });

        //start email intent when clicked
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_SUBJECT, "New Order Request");
                intent.putExtra(Intent.EXTRA_TEXT, "I would like to place an order for more " + nameTextview.getText() + ".");
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        //delete entry
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        Intent intent = getIntent();
        currentUri = intent.getData();

        //start loader
        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);

    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //delete item from database
    private void deleteItem() {
        getContentResolver().delete(currentUri, null, null);
        Toast.makeText(this, "Item deleted", Toast.LENGTH_LONG).show();
        finish();
    }

    //helper method
    private void decreaseQuantity() {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        Cursor cursor = getContentResolver().query(currentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int quantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int quantity = cursor.getInt(quantityIndex);
            if (quantity > 0) {
                quantity--;
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                getContentResolver().update(currentUri, values, null, null);
            }
        }
    }

    //helper method
    private void increaseQuantity() {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        Cursor cursor = getContentResolver().query(currentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int quantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int quantity = cursor.getInt(quantityIndex);
            quantity++;
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
            getContentResolver().update(currentUri, values, null, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        return new CursorLoader(this, currentUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {//recenter cursor
            //get column indexes
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

            //get name
            String name = cursor.getString(nameColumnIndex);

            //get price
            String price = "$";
            price += cursor.getString(priceColumnIndex);

            //get image
            String imageString = cursor.getString(imageColumnIndex);
            Uri imageUri = Uri.parse(imageString);
            itemImageView.setImageURI(imageUri);
            itemImageView.setRotation(90);

            //get quantity
            int quantity = cursor.getInt(quantityColumnIndex);

            //set data
            nameTextview.setText(name);
            priceTextview.setText(price);
            quantityTextview.setText(Integer.toString(quantity));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameTextview.setText("");
        priceTextview.setText("");
        quantityTextview.setText("");
        itemImageView.setImageResource(0);
    }
}
