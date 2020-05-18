package org.apache.cordova.sayang;


import android.content.ContentValues;

import android.content.Context;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;



/**

 * Created by asus on 02/04/2017.

 */



public class DatabaseHelper extends SQLiteOpenHelper{

  //nama database

  public static final String DATABASE_NAME = "Cordova.db";

  //nama table

  public static final String TABLE_NAME_USER = "user_table";
  public static final String TABLE_NAME_PUSHER = "pusher_table";

  //versi database

  private static final int DATABASE_VERSION = 1;

  //table field

  public static final String COL_1 = "ID";

  public static final String COL_2 = "json_data";




  public DatabaseHelper(Context context) {

    super(context, DATABASE_NAME, null, DATABASE_VERSION);


  }



  @Override

  public void onCreate(SQLiteDatabase db) {

    db.execSQL(
      "create table user_table(id integer primary key autoincrement," +
      "json_data text);");
    db.execSQL(
      "create table pusher_table(id integer primary key autoincrement," +
        "json_data text);");
  }



  @Override

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_USER);

    onCreate(db);

  }



  //metode untuk tambah data

  public boolean insertData(String json_data) {
    this.truncateData();
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues contentValues = new ContentValues();

    contentValues.put(COL_2,json_data);



    long result = db.insert(TABLE_NAME_USER, null, contentValues);

    if(result == -1)

      return false;

    else

      return true;

  }



  //metode untuk mengambil data

  public Cursor getAllData() {

    SQLiteDatabase db = this.getWritableDatabase();

    Cursor res = db.rawQuery("select * from user_table", null);

    return res;

  }



  //metode untuk merubah data

  public boolean updateData(String id,String name) {

    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues contentValues = new ContentValues();

    contentValues.put(COL_1,id);

    contentValues.put(COL_2,name);

    db.update(TABLE_NAME_USER,contentValues,"ID = ?",new String[] {id});

    return true;

  }



  //metode untuk menghapus data

  public int deleteData (String id) {

    SQLiteDatabase db = this.getWritableDatabase();

    return db.delete(TABLE_NAME_USER, "ID = ?", new String[] {id});

  }
  public void truncateData() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("DELETE FROM "+ TABLE_NAME_USER);
  }

  public boolean insertDataPusher(String json_data) {
    this.truncateDataPusher();
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues contentValues = new ContentValues();

    contentValues.put(COL_2,json_data);



    long result = db.insert(TABLE_NAME_PUSHER, null, contentValues);

    if(result == -1)

      return false;

    else

      return true;

  }



  //metode untuk mengambil data

  public Cursor getAllDataPusher() {

    SQLiteDatabase db = this.getWritableDatabase();

    Cursor res = db.rawQuery("select * from pusher_table", null);

    return res;

  }



  //metode untuk merubah data

  public boolean updateDataPusher(String id,String name) {

    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues contentValues = new ContentValues();

    contentValues.put(COL_1,id);

    contentValues.put(COL_2,name);

    db.update(TABLE_NAME_PUSHER,contentValues,"ID = ?",new String[] {id});

    return true;

  }



  //metode untuk menghapus data

  public int deleteDataPusher(String id) {

    SQLiteDatabase db = this.getWritableDatabase();

    return db.delete(TABLE_NAME_PUSHER, "ID = ?", new String[] {id});

  }
  public void truncateDataPusher() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("DELETE FROM "+ TABLE_NAME_PUSHER);
  }

}
