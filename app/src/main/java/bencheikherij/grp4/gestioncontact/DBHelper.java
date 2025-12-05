package bencheikherij.grp4.gestioncontact;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "contacts.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "contacts";
    public static final String COL_ID = "id";
    public static final String COL_NOM = "nom";
    public static final String COL_PSEUDO = "pseudo";
    public static final String COL_NUM = "numero";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOM + " TEXT, " +
                COL_PSEUDO + " TEXT, " +
                COL_NUM + " TEXT)";
        db.execSQL(sql);

        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert
    public long addContact(Contact c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOM, c.nom);
        cv.put(COL_PSEUDO, c.pseudo);
        cv.put(COL_NUM, c.numero);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    // Get all
    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY " + COL_NOM + " COLLATE NOCASE", null);
        if (cur.moveToFirst()) {
            do {
                int id = cur.getInt(cur.getColumnIndexOrThrow(COL_ID));
                String nom = cur.getString(cur.getColumnIndexOrThrow(COL_NOM));
                String pseudo = cur.getString(cur.getColumnIndexOrThrow(COL_PSEUDO));
                String num = cur.getString(cur.getColumnIndexOrThrow(COL_NUM));
                list.add(new Contact(id, nom, pseudo, num));
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return list;
    }

    // Update
    public int updateContact(Contact c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOM, c.nom);
        cv.put(COL_PSEUDO, c.pseudo);
        cv.put(COL_NUM, c.numero);
        int rows = db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(c.id)});
        db.close();
        return rows;
    }

    public Contact getContactById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Contact c = new Contact();
            c.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            c.nom = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOM));
            c.pseudo = cursor.getString(cursor.getColumnIndexOrThrow(COL_PSEUDO));
            c.numero = cursor.getString(cursor.getColumnIndexOrThrow(COL_NUM));
            cursor.close();
            return c;
        }
        return null;
    }



    // Delete
    public int deleteContact(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;   // -1 means not found
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                " WHERE " + COL_USERNAME + "=?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}



