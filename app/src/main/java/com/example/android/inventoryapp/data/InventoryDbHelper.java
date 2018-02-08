package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by JesseRawlings on 12/22/17.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create database
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                        InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                        InventoryEntry.COLUMN_ITEM_PRICE + " TEXT NOT NULL, " +
                        InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT NOT NULL, " +
                        InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothin
    }
}
