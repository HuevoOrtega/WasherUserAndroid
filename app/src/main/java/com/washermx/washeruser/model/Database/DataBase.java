package com.washermx.washeruser.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.washermx.washeruser.model.Car;
import com.washermx.washeruser.model.Service;
import com.washermx.washeruser.model.User;
import com.washermx.washeruser.model.UserCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataBase extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "Washer.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY";
    private static final String COMMA_SEP = ",";
    private static final String AND = " AND ";
    private static final String DESC = " DESC ";
    private static final String SQL_CREATE_ENTRIES_FOR_USER =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry.ID + PRIMARY_KEY + COMMA_SEP +
                    UserEntry.NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.EMAIL + TEXT_TYPE + COMMA_SEP +
                    UserEntry.PHONE + TEXT_TYPE + COMMA_SEP +
                    UserEntry.IMAGE + TEXT_TYPE + COMMA_SEP +
                    UserEntry.BILLING_NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.RFC  + TEXT_TYPE + COMMA_SEP +
                    UserEntry.BILLING_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    UserEntry.CODIGO + TEXT_TYPE +
                    " )";
    private static final String SQL_CREATE_ENTRIES_FOR_CAR =
            "CREATE TABLE " + CarEntry.TABLE_NAME + " (" +
                    CarEntry.ID + PRIMARY_KEY + " AUTOINCREMENT NOT NULL" + COMMA_SEP +
                    CarEntry.VEHICLE + TEXT_TYPE + COMMA_SEP +
                    CarEntry.COLOR + TEXT_TYPE + COMMA_SEP +
                    CarEntry.PLATES + TEXT_TYPE + COMMA_SEP +
                    CarEntry.MODEL + TEXT_TYPE + COMMA_SEP +
                    CarEntry.BRAND + TEXT_TYPE + COMMA_SEP +
                    CarEntry.FAVORITE + INTEGER_TYPE +
                    " )";
    private static final String SQL_CREATE_ENTRIES_FOR_SERVICE =
            "CREATE TABLE " + ServiceEntry.TABLE_NAME + " (" +
                    ServiceEntry.ID + PRIMARY_KEY + COMMA_SEP +
                    ServiceEntry.CAR + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.CLEANER_NAME + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.SERVICE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.PRICE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.STARTED_DATE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.LATITUD + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.LONGITUD + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.STATUS + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.RATING + INTEGER_TYPE + COMMA_SEP +
                    ServiceEntry.CLEANER_ID + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.ACCEPTED_TIME + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.METODO_PAGO + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.PRECIO_A_PAGAR + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.FINAL_TIME + TEXT_TYPE +
                    " )";
    private static final String SQL_CREATE_ENTRIES_FOR_CARD =
            "CREATE TABLE " + CardEntry.TABLE_NAME + " (" +
                    CardEntry.CARD_ID + PRIMARY_KEY + COMMA_SEP +
                    CardEntry.CARD_NUMBER + TEXT_TYPE + COMMA_SEP +
                    CardEntry.CARD_NAME + TEXT_TYPE + COMMA_SEP +
                    CardEntry.EXPIRATION_MONTH + TEXT_TYPE + COMMA_SEP +
                    CardEntry.EXPIRATION_YEAR + TEXT_TYPE + COMMA_SEP +
                    CardEntry.CVV + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES_USER =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_CAR =
            "DROP TABLE IF EXISTS " + CarEntry.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_SERVICE =
            "DROP TABLE IF EXISTS " + ServiceEntry.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_CARD =
            "DROP TABLE IF EXISTS " + CardEntry.TABLE_NAME;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_FOR_USER);
        db.execSQL(SQL_CREATE_ENTRIES_FOR_CAR);
        db.execSQL(SQL_CREATE_ENTRIES_FOR_SERVICE);
        db.execSQL(SQL_CREATE_ENTRIES_FOR_CARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_USER);
        db.execSQL(SQL_DELETE_ENTRIES_CAR);
        db.execSQL(SQL_DELETE_ENTRIES_SERVICE);
        db.execSQL(SQL_DELETE_ENTRIES_CARD);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }

    public void deleteTableUser(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(UserEntry.TABLE_NAME,null,null);
    }

    public void saveUser(User user){
        SQLiteDatabase db = getWritableDatabase();
        deleteTableUser();
        ContentValues row = new ContentValues();
        row.put(UserEntry.ID,user.id);
        row.put(UserEntry.NAME,user.name);
        row.put(UserEntry.LAST_NAME,user.lastName);
        row.put(UserEntry.EMAIL,user.email);
        row.put(UserEntry.PHONE,user.phone);
        row.put(UserEntry.IMAGE,user.imagePath);
        row.put(UserEntry.BILLING_NAME,user.billingName);
        row.put(UserEntry.RFC,user.rfc);
        row.put(UserEntry.BILLING_ADDRESS,user.billingAddress);
        row.put(UserEntry.CODIGO,user.codigo);
        db.insert(UserEntry.TABLE_NAME,null,row);
        db.close();
    }

    public User readUser(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = configureQuery(db, UserEntry.TABLE_NAME, null, null, null);
        User user = new User();
        if (cursor.moveToFirst()) {
            user.id = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.ID));
            user.name = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.NAME));
            user.lastName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.LAST_NAME));
            user.email = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.EMAIL));
            user.phone = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.PHONE));
            user.imagePath = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.IMAGE));
            user.billingName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.BILLING_NAME));
            user.rfc = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.RFC));
            user.billingAddress = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.BILLING_ADDRESS));
            user.codigo = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.CODIGO));
        }
        db.close();
        cursor.close();
        return user;
    }


    public void deleteTableCar(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CarEntry.TABLE_NAME,null,null);
    }

    public void saveCars(List<Car> cars){
        SQLiteDatabase db = getWritableDatabase();
        deleteTableCar();
        for (Car car:cars) {
            ContentValues row = new ContentValues();
            row.put(CarEntry.ID,car.id);
            row.put(CarEntry.VEHICLE,car.type);
            row.put(CarEntry.COLOR,car.color);
            row.put(CarEntry.PLATES,car.plates);
            row.put(CarEntry.BRAND,car.brand);
            row.put(CarEntry.FAVORITE,car.favorite);
            db.insert(CarEntry.TABLE_NAME,null,row);
        }
        db.close();
    }

    public List<Car> readCars(){
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CarEntry.ID + DESC;
        Cursor cursor = configureQuery(db, CarEntry.TABLE_NAME, null, null, sortOrder);
        List<Car> cars = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Car car = new Car();
                car.id = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.ID));
                car.type = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.VEHICLE));
                car.color = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.COLOR));
                car.plates = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.PLATES));
                car.brand = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.BRAND));
                car.favorite = cursor.getInt(cursor.getColumnIndexOrThrow(CarEntry.FAVORITE));
                cars.add(car);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return cars;
    }

    public Car getFavoriteCar(){
        SQLiteDatabase db = getReadableDatabase();
        String whereClause = CarEntry.FAVORITE + " == ?";
        String[] whereArgs = {
                "1"
        };
        String sortOrder = CarEntry.ID + DESC;
        Cursor cursor = configureQuery(db, CarEntry.TABLE_NAME, whereClause, whereArgs, sortOrder);
        Car car = null;
        if (cursor.moveToFirst()) {

            do {
                int fav = cursor.getInt(cursor.getColumnIndexOrThrow(CarEntry.FAVORITE));
                if (fav == 1) {
                    car = new Car();
                    car.id = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.ID));
                    car.type = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.VEHICLE));
                    car.color = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.COLOR));
                    car.plates = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.PLATES));
                    car.brand = cursor.getString(cursor.getColumnIndexOrThrow(CarEntry.BRAND));
                    car.favorite = cursor.getInt(cursor.getColumnIndexOrThrow(CarEntry.FAVORITE));
                    break;
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return car;
    }

    public void setFavoriteCar(String id){
        List<Car> cars = readCars();
        for (int i = 0; i < cars.size(); i++){
            cars.get(i).favorite = 0;
            if (cars.get(i).id.equals(id))
                cars.get(i).favorite = 1;
        }
        saveCars(cars);
    }


    public void deleteTableService(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ServiceEntry.TABLE_NAME,null,null);
    }

    public void saveServices(List<Service> services){
        SQLiteDatabase db = getWritableDatabase();
        deleteTableService();
        for (Service service:services) {
            ContentValues row = new ContentValues();
            row.put(ServiceEntry.ID,service.id);
            row.put(ServiceEntry.CAR,service.car);
            row.put(ServiceEntry.CLEANER_NAME,service.cleanerName);
            row.put(ServiceEntry.SERVICE,service.service);
            row.put(ServiceEntry.PRICE,service.price);
            row.put(ServiceEntry.DESCRIPTION,service.description);
            row.put(ServiceEntry.STARTED_DATE,service.startedTime);
            row.put(ServiceEntry.LATITUD,service.latitud);
            row.put(ServiceEntry.LONGITUD,service.longitud);
            row.put(ServiceEntry.STATUS,service.status);
            row.put(ServiceEntry.RATING,service.rating);
            row.put(ServiceEntry.CLEANER_ID,service.cleanerId);
            row.put(ServiceEntry.METODO_PAGO, service.metodoDePago);
            row.put(ServiceEntry.PRECIO_A_PAGAR, service.precioAPagar);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            if (service.finalTime != null) {
                row.put(ServiceEntry.FINAL_TIME, format.format(service.finalTime));
            }
            if (service.acceptedTime != null){
                row.put(ServiceEntry.ACCEPTED_TIME, format.format(service.acceptedTime));
            }
            db.insert(ServiceEntry.TABLE_NAME,null,row);
        }
        db.close();
    }

    public List<Service> readServices(){
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, null, null, sortOrder);
        List<Service> services = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Service service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.cleanerName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLEANER_NAME));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.rating = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceEntry.RATING));
                service.metodoDePago = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.METODO_PAGO));
                service.precioAPagar = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRECIO_A_PAGAR));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                try {
                    service.acceptedTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ACCEPTED_TIME)));
                } catch (Exception e) {
                    service.acceptedTime = null;
                }
                services.add(service);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return services;
    }

    public Service getActiveService(){
        SQLiteDatabase db = getReadableDatabase();
        String whereClause = ServiceEntry.STATUS + " != ?"+ AND + ServiceEntry.RATING + "== ?";
        String[] whereArgs = {
                "Canceled",
                "-1"
        };
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, whereClause, whereArgs, sortOrder);
        Service service = null;
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceEntry.RATING));
                if ((status.equals("Finished") && rating != -1) || status.equals("Canceled"))
                        continue;

                service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.cleanerName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLEANER_NAME));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.cleanerId = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLEANER_ID));
                service.rating = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceEntry.RATING));
                service.metodoDePago = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.METODO_PAGO));
                service.precioAPagar = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRECIO_A_PAGAR));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                try {
                    service.acceptedTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ACCEPTED_TIME)));
                } catch (Exception e) {
                    service.acceptedTime = null;
                }
                break;
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return service;
    }

    public List<Service> getFinishedServices(){
        SQLiteDatabase db = getReadableDatabase();
        String whereClause = ServiceEntry.STATUS + " == ?"+ AND + ServiceEntry.RATING + " != ?";
        String[] whereArgs = {
                "Finished",
                "-1"
        };
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, whereClause, whereArgs, sortOrder);
        List<Service> services = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceEntry.RATING));
                if (rating == -1 || !status.equals("Finished"))
                    continue;

                Service service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.cleanerName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLEANER_NAME));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.rating = cursor.getInt(cursor.getColumnIndexOrThrow(ServiceEntry.RATING));
                service.cleanerId = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLEANER_ID));
                service.metodoDePago = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.METODO_PAGO));
                service.precioAPagar = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRECIO_A_PAGAR));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                try {
                    service.acceptedTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ACCEPTED_TIME)));
                } catch (Exception e) {
                    service.acceptedTime = null;
                }
                services.add(service);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return services;
    }

    public void deleteTableCard(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CardEntry.TABLE_NAME,null,null);
    }

    public void saveCard(UserCard card) {
        SQLiteDatabase db = getWritableDatabase();
        deleteTableCard();

        ContentValues row = new ContentValues();
        row.put(CardEntry.CARD_NUMBER, card.cardNumber);
        row.put(CardEntry.EXPIRATION_MONTH, card.expirationMonth);
        row.put(CardEntry.EXPIRATION_YEAR, card.expirationYear);
        row.put(CardEntry.CVV, card.cvv);

        db.insert(CardEntry.TABLE_NAME, null, row);

        db.close();
    }

    public UserCard readCard(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = configureQuery(db, CardEntry.TABLE_NAME, null, null, null);
        UserCard card = null;
        if (cursor.moveToFirst()) {
            card = new UserCard();
            card.cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(CardEntry.CARD_NUMBER));
            card.expirationMonth = cursor.getString(cursor.getColumnIndexOrThrow(CardEntry.EXPIRATION_MONTH));
            card.expirationYear = cursor.getString(cursor.getColumnIndexOrThrow(CardEntry.EXPIRATION_YEAR));
            card.cvv = cursor.getString(cursor.getColumnIndexOrThrow(CardEntry.CVV));
        }
        db.close();
        cursor.close();
        return card;
    }


    private Cursor configureQuery(SQLiteDatabase db,String table, String whereClause,String[] whereArgs, String sortOrder) {
        return db.query(
                table,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                sortOrder
        );
    }

    static abstract class UserEntry implements BaseColumns{
        static final String TABLE_NAME = "User";
        static final String ID = "id";
        static final String NAME = "name";
        static final String LAST_NAME = "lastName";
        static final String EMAIL = "email";
        static final String PHONE = "phone";
        static final String IMAGE = "image";
        static final String BILLING_NAME = "billingName";
        static final String RFC = "rfc";
        static final String BILLING_ADDRESS = "billingAddress";
        static final String CODIGO = "CODIGO";
    }

    static abstract class CarEntry implements BaseColumns{
        static final String TABLE_NAME = "Car";
        static final String ID = "id";
        static final String VEHICLE = "vehicle";
        static final String COLOR = "color";
        static final String PLATES = "plates";
        static final String MODEL = "model";
        static final String BRAND = "brand";
        static final String FAVORITE = "favorite";
    }

    static abstract class ServiceEntry implements BaseColumns{
        static final String TABLE_NAME = "Service";
        static final String ID = "id";
        static final String CAR = "car";
        static final String CLEANER_NAME = "cleanerName";
        static final String SERVICE = "service";
        static final String PRICE = "price";
        static final String DESCRIPTION = "description";
        static final String STARTED_DATE = "startedDate";
        static final String LATITUD = "latitud";
        static final String LONGITUD = "longitud";
        static final String STATUS = "status";
        static final String RATING = "rating";
        static final String CLEANER_ID = "cleanerId";
        static final String ACCEPTED_TIME = "acceptedTime";
        static final String FINAL_TIME = "finalTime";
        static final String METODO_PAGO = "metodoPago";
        static final String PRECIO_A_PAGAR = "precioAPagar";
    }

    static abstract class CardEntry implements BaseColumns{
        static final String TABLE_NAME = "Card";
        static final String CARD_ID = "id";
        static final String CARD_NUMBER = "cardNumber";
        static final String CARD_NAME = "cardName";
        static final String EXPIRATION_MONTH = "expirationMonth";
        static final String EXPIRATION_YEAR = "expirationYear";
        static final String CVV = "cvv";
    }
}
