package com.rentease.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rentease.app.model.Booking;
import com.rentease.app.model.Property;
import com.rentease.app.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rentease.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_PHONE = "phone";

    // Table: Properties
    public static final String TABLE_PROPERTIES = "properties";
    public static final String COL_PROP_ID = "id";
    public static final String COL_PROP_NAME = "name";
    public static final String COL_PROP_LOCATION = "location";
    public static final String COL_PROP_TYPE = "type";
    public static final String COL_PROP_RENT = "rent";
    public static final String COL_PROP_DESCRIPTION = "description";
    public static final String COL_PROP_OWNER_ID = "owner_id";
    public static final String COL_PROP_IMAGE_URL = "image_url";
    public static final String COL_PROP_STATUS = "status";

    // Table: Bookings
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_PROP_ID = "property_id";
    public static final String COL_BOOK_PROP_NAME = "property_name";
    public static final String COL_BOOK_USER_ID = "user_id";
    public static final String COL_BOOK_USER_NAME = "user_name";
    public static final String COL_BOOK_PHONE = "phone";
    public static final String COL_BOOK_MOVE_IN = "move_in_date";
    public static final String COL_BOOK_MESSAGE = "message";
    public static final String COL_BOOK_STATUS = "status";

    // Table: Messages
    public static final String TABLE_MESSAGES = "messages";
    public static final String COL_MSG_ID = "id";
    public static final String COL_MSG_SENDER = "sender_name";
    public static final String COL_MSG_TEXT = "text";
    public static final String COL_MSG_TIME = "timestamp";

    private static DatabaseHelper instance;
    private final Context context;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context != null ? context.getApplicationContext() : null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT, "
                + COL_USER_EMAIL + " TEXT UNIQUE, "
                + COL_USER_PASSWORD + " TEXT, "
                + COL_USER_PHONE + " TEXT)";
        db.execSQL(createUsersTable);

        // Create Properties table
        String createPropertiesTable = "CREATE TABLE " + TABLE_PROPERTIES + " ("
                + COL_PROP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROP_NAME + " TEXT, "
                + COL_PROP_LOCATION + " TEXT, "
                + COL_PROP_TYPE + " TEXT, "
                + COL_PROP_RENT + " TEXT, "
                + COL_PROP_DESCRIPTION + " TEXT, "
                + COL_PROP_OWNER_ID + " TEXT, "
                + COL_PROP_IMAGE_URL + " TEXT, "
                + COL_PROP_STATUS + " TEXT DEFAULT 'Active')";
        db.execSQL(createPropertiesTable);

        // Create Bookings table
        String createBookingsTable = "CREATE TABLE " + TABLE_BOOKINGS + " ("
                + COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_BOOK_PROP_ID + " TEXT, "
                + COL_BOOK_PROP_NAME + " TEXT, "
                + COL_BOOK_USER_ID + " TEXT, "
                + COL_BOOK_USER_NAME + " TEXT, "
                + COL_BOOK_PHONE + " TEXT, "
                + COL_BOOK_MOVE_IN + " TEXT, "
                + COL_BOOK_MESSAGE + " TEXT, "
                + COL_BOOK_STATUS + " TEXT)";
        db.execSQL(createBookingsTable);

        // Create Messages table
        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MSG_SENDER + " TEXT, "
                + COL_MSG_TEXT + " TEXT, "
                + COL_MSG_TIME + " INTEGER)";
        db.execSQL(createMessagesTable);

        // Seed initial sample data
        seedInitialData(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        try {
            db.execSQL("ALTER TABLE " + TABLE_PROPERTIES + " ADD COLUMN " + COL_PROP_STATUS + " TEXT DEFAULT 'Active'");
        } catch (Exception ignored) {
            // Column already exists
        }
        try {
            // Ensure Mumbai mockup properties exist (1 BHK, 2 BHK, 3 BHK, 4 BHK, Bungalow, Villa, Studio, Gala)
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROPERTIES + " WHERE " + COL_PROP_NAME + " LIKE '%Bungalow%' OR " + COL_PROP_NAME + " LIKE '%Gala%'", null);
            boolean hasExtendedProps = false;
            if (c != null) {
                if (c.moveToFirst() && c.getInt(0) > 0) hasExtendedProps = true;
                c.close();
            }
            if (!hasExtendedProps) {
                insertPropertyDirect(db, "1 BHK Cozy Apartment", "Borivali East, Mumbai", "1 BHK (Rent)", "18,000",
                        "Cozy 1 BHK flat near Borivali station, well connected, 24h water and security.", "system", "prop_real_1bhk");
                insertPropertyDirect(db, "2 BHK Modern Apartment", "Andheri West, Mumbai", "2 BHK (Rent)", "28,000",
                        "Spacious 2 BHK apartment with modular kitchen, 24x7 water supply, parking and power backup.", "system", "prop_real_2bhk");
                insertPropertyDirect(db, "3 BHK Luxury Flat", "Malad West, Mumbai", "3 BHK (Sale/Rent)", "42,000",
                        "Luxurious 3 BHK apartment with clubhouse, pool, gym, covered parking.", "system", "prop_real_3bhk");
                insertPropertyDirect(db, "4 BHK Luxury Penthouse", "Bandra West, Mumbai", "4 BHK (Sale)", "2,85,00,000",
                        "Sea-facing 4 BHK luxury penthouse with private terrace, Italian marble, smart home automation.", "system", "prop_real_4bhk");
                insertPropertyDirect(db, "Independent Royal Bungalow", "Lonavala / Khandala", "Bungalow (Sale)", "3,50,00,000",
                        "Grand 4-bedroom bungalow with private lawn, mountain view, garden, gated security and gazebo.", "system", "prop_real_bungalow");
                insertPropertyDirect(db, "Luxury Palm Villa with Pool", "Alibaug Beach Road", "Villa (Rent)", "1,10,000",
                        "Private luxury villa with private swimming pool, landscaped lawn, outdoor seating and barbecue deck.", "system", "prop_real_villa");
                insertPropertyDirect(db, "Modern Studio Apartment", "Powai, Mumbai", "Studio (Rent)", "15,000",
                        "Fully furnished studio apartment walking distance from tech park, cafes and lake.", "system", "prop_real_studio");
                insertPropertyDirect(db, "Commercial Shop / Gala", "Vashi Sector 17, Navi Mumbai", "Commercial Gala (Rent)", "35,000",
                        "Prime ground floor commercial gala / retail shop with glass facade, high footfall, ideal for office or showroom.", "system", "prop_real_gala");
            }

            // Update all property image URLs to dedicated local high-res drawables so they never fail to load
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_1bhk' WHERE " + COL_PROP_NAME + " LIKE '%1 BHK%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_2bhk' WHERE " + COL_PROP_NAME + " LIKE '%2 BHK%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_3bhk' WHERE " + COL_PROP_NAME + " LIKE '%3 BHK%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_4bhk' WHERE " + COL_PROP_NAME + " LIKE '%4 BHK%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_bungalow' WHERE " + COL_PROP_NAME + " LIKE '%Bungalow%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_villa' WHERE " + COL_PROP_NAME + " LIKE '%Villa%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_studio' WHERE " + COL_PROP_NAME + " LIKE '%Studio%'");
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_IMAGE_URL + " = 'prop_real_gala' WHERE " + COL_PROP_NAME + " LIKE '%Gala%' OR " + COL_PROP_NAME + " LIKE '%Shop%'");

            // Reassign any sample properties owned by '1' to 'system' so My Properties is strictly for user-created properties
            db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_OWNER_ID + " = 'system' WHERE " + COL_PROP_OWNER_ID + " = '1'");

            // Clean/renew database on initial update as requested by user
            if (context != null) {
                android.content.SharedPreferences p = context.getSharedPreferences("RentEaseDbPrefs", Context.MODE_PRIVATE);
                if (!p.getBoolean("fresh_clean_db_v5", false)) {
                    db.execSQL("DELETE FROM " + TABLE_BOOKINGS);
                    db.execSQL("UPDATE " + TABLE_PROPERTIES + " SET " + COL_PROP_STATUS + " = 'Active'");
                    p.edit().putBoolean("fresh_clean_db_v5", true).apply();
                }
            }
            
            // Ensure mockup messages exist
            Cursor cm = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + COL_MSG_SENDER + " = 'Anita Verma'", null);
            boolean hasMockupMsgs = false;
            if (cm != null) {
                if (cm.moveToFirst() && cm.getInt(0) > 0) hasMockupMsgs = true;
                cm.close();
            }
            if (!hasMockupMsgs) {
                insertMessageDirect(db, "Anita Verma", "Hello, is this property available?");
                insertMessageDirect(db, "Rahul Mehta", "I would like to visit the property.");
                insertMessageDirect(db, "Owner - 2 BHK Apartment", "Your booking request is confirmed.");
                insertMessageDirect(db, "Priya Singh", "Can we schedule a visit?");
            }
        } catch (Exception ignored) {
        }
    }

    private void insertMessageDirect(SQLiteDatabase db, String sender, String text) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_SENDER, sender);
        cv.put(COL_MSG_TEXT, text);
        cv.put(COL_MSG_TIME, System.currentTimeMillis());
        db.insert(TABLE_MESSAGES, null, cv);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Sample user
        ContentValues userVal = new ContentValues();
        userVal.put(COL_USER_NAME, "Rohit Sharma");
        userVal.put(COL_USER_EMAIL, "rohit.sharma@email.com");
        userVal.put(COL_USER_PASSWORD, "password123");
        userVal.put(COL_USER_PHONE, "9876543210");
        db.insert(TABLE_USERS, null, userVal);

        // Sample properties matching user's request (1 BHK, 2 BHK, 3 BHK, 4 BHK, Bungalow, Villa, Studio, Commercial Gala)
        // Owned by 'system' so they show on Home feed, but NOT in any user's personal "My Properties"
        insertPropertyDirect(db, "1 BHK Cozy Apartment", "Borivali East, Mumbai", "1 BHK (Rent)", "18,000",
                "Cozy 1 BHK flat near Borivali station, well connected, 24h water and security.", "system", "prop_real_1bhk");

        insertPropertyDirect(db, "2 BHK Modern Apartment", "Andheri West, Mumbai", "2 BHK (Rent)", "28,000",
                "Spacious 2 BHK apartment with modular kitchen, 24x7 water supply, parking and power backup.", "system", "prop_real_2bhk");

        insertPropertyDirect(db, "3 BHK Luxury Flat", "Malad West, Mumbai", "3 BHK (Sale/Rent)", "42,000",
                "Luxurious 3 BHK apartment with clubhouse, pool, gym, covered parking.", "system", "prop_real_3bhk");

        insertPropertyDirect(db, "4 BHK Luxury Penthouse", "Bandra West, Mumbai", "4 BHK (Sale)", "2,85,00,000",
                "Sea-facing 4 BHK luxury penthouse with private terrace, Italian marble, smart home automation.", "system", "prop_real_4bhk");

        insertPropertyDirect(db, "Independent Royal Bungalow", "Lonavala / Khandala", "Bungalow (Sale)", "3,50,00,000",
                "Grand 4-bedroom bungalow with private lawn, mountain view, garden, gated security and gazebo.", "system", "prop_real_bungalow");

        insertPropertyDirect(db, "Luxury Palm Villa with Pool", "Alibaug Beach Road", "Villa (Rent)", "1,10,000",
                "Private luxury villa with private swimming pool, landscaped lawn, outdoor seating and barbecue deck.", "system", "prop_real_villa");

        insertPropertyDirect(db, "Modern Studio Apartment", "Powai, Mumbai", "Studio (Rent)", "15,000",
                "Fully furnished studio apartment walking distance from tech park, cafes and lake.", "system", "prop_real_studio");

        insertPropertyDirect(db, "Commercial Shop / Gala", "Vashi Sector 17, Navi Mumbai", "Commercial Gala (Rent)", "35,000",
                "Prime ground floor commercial gala / retail shop with glass facade, high footfall, ideal for office or showroom.", "system", "prop_real_gala");

        // Sample messages matching user's mockup Screen 9
        insertMessageDirect(db, "Anita Verma", "Hello, is this property available?");
        insertMessageDirect(db, "Rahul Mehta", "I would like to visit the property.");
        insertMessageDirect(db, "Owner - 2 BHK Apartment", "Your booking request is confirmed.");
        insertMessageDirect(db, "Priya Singh", "Can we schedule a visit?");
    }

    private void insertBookingDirect(SQLiteDatabase db, String propId, String propName, String userId,
                                     String userName, String phone, String moveInDate, String msg, String status) {
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_PROP_ID, propId);
        cv.put(COL_BOOK_PROP_NAME, propName);
        cv.put(COL_BOOK_USER_ID, userId);
        cv.put(COL_BOOK_USER_NAME, userName);
        cv.put(COL_BOOK_PHONE, phone);
        cv.put(COL_BOOK_MOVE_IN, moveInDate);
        cv.put(COL_BOOK_MESSAGE, msg);
        cv.put(COL_BOOK_STATUS, status);
        db.insert(TABLE_BOOKINGS, null, cv);
    }

    private void insertPropertyDirect(SQLiteDatabase db, String name, String loc, String type,
                                      String rent, String desc, String ownerId, String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PROP_NAME, name);
        cv.put(COL_PROP_LOCATION, loc);
        cv.put(COL_PROP_TYPE, type);
        cv.put(COL_PROP_RENT, rent);
        cv.put(COL_PROP_DESCRIPTION, desc);
        cv.put(COL_PROP_OWNER_ID, ownerId);
        cv.put(COL_PROP_IMAGE_URL, imageUrl);
        cv.put(COL_PROP_STATUS, "Active");
        db.insert(TABLE_PROPERTIES, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPERTIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // ==========================================
    // USER AUTHENTICATION & OPERATIONS
    // ==========================================

    public long registerUser(String name, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_EMAIL, email.trim().toLowerCase());
        cv.put(COL_USER_PASSWORD, password);
        cv.put(COL_USER_PHONE, phone);
        return db.insert(TABLE_USERS, null, cv);
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_EMAIL + " = ?", new String[]{email.trim().toLowerCase()},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public User checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{email.trim().toLowerCase(), password},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + " = ?",
                new String[]{email.trim().toLowerCase()},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public int updateUser(String id, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_PHONE, phone);
        return db.update(TABLE_USERS, cv, COL_USER_ID + " = ?", new String[]{id});
    }

    public boolean updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, cv, COL_USER_EMAIL + " = ?", new String[]{email.trim().toLowerCase()});
        return rows > 0;
    }

    public boolean updatePasswordById(String id, String newPassword) {
        if (id == null || newPassword == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    // DELETE USER FROM SQLITE
    public int deleteUser(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{id});
    }

    public int deleteUserByEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COL_USER_EMAIL + " = ?", new String[]{email.trim().toLowerCase()});
    }

    private User cursorToUser(Cursor c) {
        User u = new User();
        u.setId(c.getString(c.getColumnIndexOrThrow(COL_USER_ID)));
        u.setName(c.getString(c.getColumnIndexOrThrow(COL_USER_NAME)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(COL_USER_EMAIL)));
        u.setPassword(c.getString(c.getColumnIndexOrThrow(COL_USER_PASSWORD)));
        u.setPhone(c.getString(c.getColumnIndexOrThrow(COL_USER_PHONE)));
        return u;
    }

    // ==========================================
    // PROPERTY CRUD OPERATIONS
    // ==========================================

    // CREATE (Insert)
    public long insertProperty(Property property) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROP_NAME, property.name);
        cv.put(COL_PROP_LOCATION, property.location);
        cv.put(COL_PROP_TYPE, property.type);
        cv.put(COL_PROP_RENT, property.rent);
        cv.put(COL_PROP_DESCRIPTION, property.description);
        cv.put(COL_PROP_OWNER_ID, property.ownerId);
        cv.put(COL_PROP_IMAGE_URL, property.imageUrl != null ? property.imageUrl : "");
        cv.put(COL_PROP_STATUS, property.status != null ? property.status : "Active");
        return db.insert(TABLE_PROPERTIES, null, cv);
    }

    // READ (All)
    public List<Property> getAllProperties() {
        List<Property> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROPERTIES + " ORDER BY " + COL_PROP_ID + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // READ (By ID)
    public Property getPropertyById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROPERTIES, null,
                COL_PROP_ID + " = ?", new String[]{id.trim()},
                null, null, null);

        Property property = null;
        if (cursor != null && cursor.moveToFirst()) {
            property = cursorToProperty(cursor);
            cursor.close();
        }
        return property;
    }

    // READ (By Name)
    public Property getPropertyByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanName = name.trim();

        // 1. Try direct exact or substring match
        Cursor cursor = db.query(TABLE_PROPERTIES, null,
                COL_PROP_NAME + " LIKE ? OR ? LIKE ('%' || " + COL_PROP_NAME + " || '%')",
                new String[]{"%" + cleanName + "%", cleanName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Property property = cursorToProperty(cursor);
            cursor.close();
            return property;
        }
        if (cursor != null) cursor.close();

        // 2. If name contains '-', try matching the main title before '-'
        if (cleanName.contains("-")) {
            String titlePart = cleanName.split("-")[0].trim();
            if (!titlePart.isEmpty()) {
                cursor = db.query(TABLE_PROPERTIES, null,
                        COL_PROP_NAME + " LIKE ? OR ? LIKE ('%' || " + COL_PROP_NAME + " || '%')",
                        new String[]{"%" + titlePart + "%", titlePart},
                        null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    Property property = cursorToProperty(cursor);
                    cursor.close();
                    return property;
                }
                if (cursor != null) cursor.close();
            }
        }

        // 3. Match by individual significant keywords (e.g. "2 BHK", "Studio", "Villa")
        String[] keywords = cleanName.split(" ");
        for (String kw : keywords) {
            kw = kw.trim();
            if (kw.length() >= 3) {
                cursor = db.query(TABLE_PROPERTIES, null,
                        COL_PROP_NAME + " LIKE ? OR " + COL_PROP_TYPE + " LIKE ?",
                        new String[]{"%" + kw + "%", "%" + kw + "%"},
                        null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    Property property = cursorToProperty(cursor);
                    cursor.close();
                    return property;
                }
                if (cursor != null) cursor.close();
            }
        }

        // 4. Guaranteed fallback to first available property so details never fail
        List<Property> all = getAllProperties();
        if (!all.isEmpty()) {
            return all.get(0);
        }

        return null;
    }

    // READ (Search Query)
    public List<Property> searchProperties(String query) {
        List<Property> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllProperties();
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String pattern = "%" + query.trim() + "%";
        String selection = COL_PROP_NAME + " LIKE ? OR "
                + COL_PROP_LOCATION + " LIKE ? OR "
                + COL_PROP_TYPE + " LIKE ? OR "
                + COL_PROP_DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[]{pattern, pattern, pattern, pattern};

        Cursor cursor = db.query(TABLE_PROPERTIES, null, selection, selectionArgs,
                null, null, COL_PROP_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // READ (By Owner)
    public List<Property> getPropertiesByOwner(String ownerId) {
        List<Property> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROPERTIES, null,
                COL_PROP_OWNER_ID + " = ?", new String[]{ownerId},
                null, null, COL_PROP_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // UPDATE
    public int updateProperty(Property property) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROP_NAME, property.name);
        cv.put(COL_PROP_LOCATION, property.location);
        cv.put(COL_PROP_TYPE, property.type);
        cv.put(COL_PROP_RENT, property.rent);
        cv.put(COL_PROP_DESCRIPTION, property.description);
        if (property.ownerId != null) {
            cv.put(COL_PROP_OWNER_ID, property.ownerId);
        }
        if (property.imageUrl != null) {
            cv.put(COL_PROP_IMAGE_URL, property.imageUrl);
        }
        if (property.status != null) {
            cv.put(COL_PROP_STATUS, property.status);
        }
        return db.update(TABLE_PROPERTIES, cv, COL_PROP_ID + " = ?", new String[]{property.id});
    }

    // FAST STATUS UPDATE (Expire / Activate)
    public int updatePropertyStatus(String id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROP_STATUS, status);
        return db.update(TABLE_PROPERTIES, cv, COL_PROP_ID + " = ?", new String[]{id});
    }

    public int expireProperty(String id) {
        return updatePropertyStatus(id, "Expired");
    }

    public int activateProperty(String id) {
        return updatePropertyStatus(id, "Active");
    }

    // DELETE
    public int deleteProperty(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PROPERTIES, COL_PROP_ID + " = ?", new String[]{id});
    }

    private Property cursorToProperty(Cursor c) {
        Property p = new Property();
        p.id = String.valueOf(c.getLong(c.getColumnIndexOrThrow(COL_PROP_ID)));
        p.name = c.getString(c.getColumnIndexOrThrow(COL_PROP_NAME));
        p.location = c.getString(c.getColumnIndexOrThrow(COL_PROP_LOCATION));
        p.type = c.getString(c.getColumnIndexOrThrow(COL_PROP_TYPE));
        p.rent = c.getString(c.getColumnIndexOrThrow(COL_PROP_RENT));
        p.description = c.getString(c.getColumnIndexOrThrow(COL_PROP_DESCRIPTION));
        p.ownerId = c.getString(c.getColumnIndexOrThrow(COL_PROP_OWNER_ID));
        p.imageUrl = c.getString(c.getColumnIndexOrThrow(COL_PROP_IMAGE_URL));
        int statusIdx = c.getColumnIndex(COL_PROP_STATUS);
        if (statusIdx != -1) {
            p.status = c.getString(statusIdx);
        }
        if (p.status == null || p.status.isEmpty()) {
            p.status = "Active";
        }
        return p;
    }

    // ==========================================
    // BOOKING OPERATIONS
    // ==========================================

    public boolean isPropertyBooked(String propertyId, String propertyName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            if (propertyId != null && !propertyId.trim().isEmpty()) {
                c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOK_PROP_ID + " = ? AND " + COL_BOOK_STATUS + " != 'Cancelled'", new String[]{propertyId.trim()});
                if (c != null && c.moveToFirst() && c.getInt(0) > 0) return true;
                if (c != null) c.close();
            }
            if (propertyName != null && !propertyName.trim().isEmpty()) {
                c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOK_PROP_NAME + " = ? AND " + COL_BOOK_STATUS + " != 'Cancelled'", new String[]{propertyName.trim()});
                if (c != null && c.moveToFirst() && c.getInt(0) > 0) return true;
                if (c != null) c.close();
            }
            // Also check property status in TABLE_PROPERTIES
            if (propertyId != null && !propertyId.trim().isEmpty()) {
                c = db.rawQuery("SELECT " + COL_PROP_STATUS + " FROM " + TABLE_PROPERTIES + " WHERE " + COL_PROP_ID + " = ?", new String[]{propertyId.trim()});
                if (c != null && c.moveToFirst()) {
                    String st = c.getString(0);
                    if ("Booked".equalsIgnoreCase(st)) return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
        return false;
    }

    public long insertBooking(Booking booking) {
        if (booking == null) return -1;
        // Strictly prevent double booking
        if (isPropertyBooked(booking.propertyId, booking.propertyName)) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_PROP_ID, booking.propertyId != null ? booking.propertyId : "");
        cv.put(COL_BOOK_PROP_NAME, booking.propertyName != null ? booking.propertyName : "");
        cv.put(COL_BOOK_USER_ID, booking.userId != null ? booking.userId : "");
        cv.put(COL_BOOK_USER_NAME, booking.userName != null ? booking.userName : "");
        cv.put(COL_BOOK_PHONE, booking.phone != null ? booking.phone : "");
        cv.put(COL_BOOK_MOVE_IN, booking.moveInDate != null ? booking.moveInDate : "");
        cv.put(COL_BOOK_MESSAGE, booking.message != null ? booking.message : "");
        cv.put(COL_BOOK_STATUS, booking.status != null ? booking.status : "Confirmed");
        long id = db.insert(TABLE_BOOKINGS, null, cv);

        if (id > 0) {
            // Update property status in TABLE_PROPERTIES to "Booked"
            ContentValues pcv = new ContentValues();
            pcv.put(COL_PROP_STATUS, "Booked");
            if (booking.propertyId != null && !booking.propertyId.isEmpty()) {
                db.update(TABLE_PROPERTIES, pcv, COL_PROP_ID + " = ?", new String[]{booking.propertyId});
            }
            if (booking.propertyName != null && !booking.propertyName.isEmpty()) {
                db.update(TABLE_PROPERTIES, pcv, COL_PROP_NAME + " = ?", new String[]{booking.propertyName});
            }
        }
        return id;
    }

    public List<Booking> getBookingsByUserId(String userId) {
        List<Booking> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKINGS, null,
                COL_BOOK_USER_ID + " = ?", new String[]{userId},
                null, null, COL_BOOK_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Booking> getBookingsByUserName(String userName) {
        List<Booking> list = new ArrayList<>();
        if (userName == null || userName.trim().isEmpty()) return list;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKINGS, null,
                COL_BOOK_USER_NAME + " LIKE ?", new String[]{"%" + userName.trim() + "%"},
                null, null, COL_BOOK_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKINGS, null,
                null, null,
                null, null, COL_BOOK_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private Booking cursorToBooking(Cursor cursor) {
        Booking b = new Booking();
        b.id = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOK_ID)));
        b.propertyId = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_PROP_ID));
        b.propertyName = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_PROP_NAME));
        b.userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_USER_ID));
        b.userName = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_USER_NAME));
        b.phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_PHONE));
        b.moveInDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_MOVE_IN));
        b.message = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_MESSAGE));
        b.status = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_STATUS));
        return b;
    }

    public int deleteBooking(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String propId = null, propName = null;
        Cursor c = db.rawQuery("SELECT " + COL_BOOK_PROP_ID + ", " + COL_BOOK_PROP_NAME + " FROM " + TABLE_BOOKINGS + " WHERE " + COL_BOOK_ID + " = ?", new String[]{id});
        if (c != null && c.moveToFirst()) {
            propId = c.getString(0);
            propName = c.getString(1);
            c.close();
        }
        int rows = db.delete(TABLE_BOOKINGS, COL_BOOK_ID + " = ?", new String[]{id});
        if (rows > 0) {
            ContentValues cv = new ContentValues();
            cv.put(COL_PROP_STATUS, "Active");
            if (propId != null && !propId.isEmpty()) {
                db.update(TABLE_PROPERTIES, cv, COL_PROP_ID + " = ?", new String[]{propId});
            }
            if (propName != null && !propName.isEmpty()) {
                db.update(TABLE_PROPERTIES, cv, COL_PROP_NAME + " = ?", new String[]{propName});
            }
        }
        return rows;
    }

    public int updateBookingStatus(String id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_STATUS, status);
        return db.update(TABLE_BOOKINGS, cv, COL_BOOK_ID + " = ?", new String[]{id});
    }

    // ==========================================
    // MESSAGE OPERATIONS
    // ==========================================

    public long insertMessage(String senderName, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_SENDER, senderName);
        cv.put(COL_MSG_TEXT, text);
        cv.put(COL_MSG_TIME, System.currentTimeMillis());
        return db.insert(TABLE_MESSAGES, null, cv);
    }

    public List<Map<String, String>> getAllMessages() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COL_MSG_TIME + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MSG_ID))));
                map.put("senderName", cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_SENDER)));
                map.put("text", cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TEXT)));
                list.add(map);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
}
