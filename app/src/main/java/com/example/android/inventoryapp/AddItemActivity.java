package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by JesseRawlings on 12/22/17.
 */

public class AddItemActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;

    private InventoryDbHelper mDbHelper;

    //private views

    private ImageView mItemImageView;
    private Uri mImageUri;

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;

    private Button mSelectPictureButton;
    private Button mCancelButton;
    private Button mSaveButton;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = AddItemActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        //intialize views
        mItemImageView = (ImageView) findViewById(R.id.item_image);

        mNameEditText = (EditText) findViewById(R.id.name_editText);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_editText);
        mPriceEditText = (EditText) findViewById(R.id.price_editText);
        mSelectPictureButton = (Button) findViewById(R.id.select_picture_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        mDbHelper = new InventoryDbHelper(this);


        //return immediately to last activity
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //perform checks then save
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (performChecksOnFields()) {
                    saveItemToDb();
                    Toast.makeText(AddItemActivity.this, R.string.item_add_successful, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddItemActivity.this, R.string.item_add_needs_more_info, Toast.LENGTH_LONG).show();
                }
            }
        });

        //start intent to grab picture
        mSelectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

    }

    //result code after grabbing picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mItemImageView.setImageBitmap(getBitmapFromUri(mImageUri));
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = mItemImageView.getWidth();
        int targetH = mItemImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            this.grantUriPermission(this.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    //save item once fields have been checked
    private void saveItemToDb() {

        //add item to database
        String name = mNameEditText.getText().toString();
        String price = mPriceEditText.getText().toString();

        String imageString = mImageUri.toString();

        String quantityString = mQuantityEditText.getText().toString();
        int quantity = Integer.parseInt(quantityString);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, name);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imageString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (uri == null) {
            Toast.makeText(this, R.string.item_insert_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.item_insert_successful, Toast.LENGTH_SHORT).show();
        }

    }

    //make sure all fields have data before proceeding
    private boolean performChecksOnFields() {

        boolean allFieldsHaveData = true;

        String name = mNameEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();
        String price = mPriceEditText.getText().toString();

        if (name.length() == 0) {
            allFieldsHaveData = false;
        }
        if (quantity.length() == 0) {
            allFieldsHaveData = false;
        }
        if (price.length() == 0) {
            allFieldsHaveData = false;
        }
        if (mImageUri == null) {
            allFieldsHaveData = false;
        }

        return allFieldsHaveData;

    }
}
