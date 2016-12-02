package com.alan.washer.washeruser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.alan.washer.washeruser.model.AppData;
import com.alan.washer.washeruser.model.Car;
import com.alan.washer.washeruser.model.Cleaner;
import com.alan.washer.washeruser.model.Database.DataBase;
import com.alan.washer.washeruser.model.Service;
import com.alan.washer.washeruser.model.User;
import com.alan.washer.washeruser.model.UserCard;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationDrawer extends AppCompatActivity implements View.OnClickListener, LocationListener, OnMapReadyCallback, TextView.OnEditorActionListener {

    private DrawerLayout drawerLayout;
    private ArrayList<String> navigationItems = new ArrayList<>();
    private ArrayList<Pair<String,Drawable>> listItems = new ArrayList<>();
    private static final int PAYMENT = 1;
    private static final int BILLING = 2;
    private static final int HISTORY = 3;
    private static final int CARS = 4;
    private static final int HELP = 5;
    private static final int BE_PART_OF_TEAM = 6;
    private static final int CONFIGURATION = 7;
    private static final int ABOUT = 8;
    Handler handler = new Handler(Looper.getMainLooper());
    private int viewState;
    private static final int STANDBY = 0;
    private static final int VEHICLE_SELECTED = 1;
    private static final int ECO_OR_TRADITIONAL_SELECTED = 2;
    private static final int OUTSIDE_OR_INSIDE_SELECTED = 3;
    private static final int SERVICE_START = 4;
    LinearLayout upLayout;
    LinearLayout lowLayout;
    LinearLayout startLayout;
    LinearLayout rightLayout;
    LinearLayout leftLayout;
    TextView leftButton;
    ImageView leftImage;
    TextView leftDescription;
    TextView rightButton;
    ImageView rightImage;
    TextView rightDescription;
    TextView cleanerInfo;
    ImageView cleanerImageInfo;
    TextView serviceInfo;
    String service;
    String serviceType;
    String vehicleType;
    Cleaner cleaner;
    Boolean serviceRequestedFlag = false;
    /*
     * Map
     */
    private final static int ONE_SECOND = 1000;
    private GoogleMap map;
    LatLng requestLocation = new LatLng(0, 0);
    EditText serviceLocationText;
    TextView locationText;
    Marker centralMarker;
    Marker cleanerMarker;
    List<Cleaner> cleaners = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();
    LocationManager locationManager;
    ImageView vehiclesButton;
    /*
     * Timers
     */
    Timer nearbyCleanersTimer;
    Timer reloadMapTimer;
    Timer reloadAddressTimer;
    Timer clock;
    Timer cancelAlarmClock;
    /*
     * Service
     */
    SharedPreferences settings;
    Service activeService;
    String idClient;
    User user;
    UserCard creditCard = null;
    Button cancelButton;
    Thread activeServiceCycleThread;
    String token;
    Boolean cancelSent = false;
    public static AppCompatActivity instance;
    int cancelCode = 0;
    Boolean showCancelAlert = false;
    AlertDialog requestingAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        initView();
        initLocation();
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cleaners.clear();
        initValues();
        configureServices();
        readUserImage();
        initTimers();
        AppData.saveInBackground(settings,false);
        if (showCancelAlert){
            buildAlertForCancel();
            showCancelAlert = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimers();
        AppData.saveInBackground(settings,true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimers();
    }

    public void startActiveServiceCycle() {
        if (activeServiceCycleThread == null || !activeServiceCycleThread.isAlive()) {
            activeServiceCycleThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    activeServiceCycle();
                }
            });
            activeServiceCycleThread.start();
        }
    }

    public void activeServiceCycle(){
        DataBase db = new DataBase(getBaseContext());
        while((activeService = db.getActiveService()) != null) {
            configureActiveServiceView();
            while (!settings.getBoolean(AppData.SERVICE_CHANGED,false));
        }
        configureServiceForDelete();
        checkNotification();
    }
    private void configureActiveServiceView() {
        checkNotification();
        switch (activeService.status) {
            case "Looking":
                configureActiveServiceForLooking();
                break;
            case "Accepted":
                Calendar cal = Calendar.getInstance();
                cal.setTime(activeService.acceptedTime);
                cal.add(Calendar.MINUTE, 2);
                long diffInMillis = Service.getDifferenceTimeInMillis(cal.getTime());
                if (diffInMillis < 0) {
                    diffInMillis = 0;
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                cancelCode = 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelButton.setVisibility(View.GONE);
                    }
                },diffInMillis);
                configureActiveService(getString(R.string.accepted));
                cancelAlarmClock = new Timer();
                cancelAlarmClock.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (activeService == null || cancelSent)
                                    return;
                                cancelCode = 2;
                                buildAlertForCancel();
                                if (settings.getBoolean(AppData.IN_BACKGROUND,false)) {
                                    ServiceStatusNotification.notify(getBaseContext(), getString(R.string.wish_cancel), NavigationDrawer.class);
                                    showCancelAlert = true;
                                }
                            }
                        });
                    }
                },ONE_SECOND*60*15);
                break;
            case "On The Way":
                if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                configureActiveService(getString(R.string.on_the_way));
                break;
            case "Started":
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelButton.setVisibility(View.GONE);
                    }
                });
                if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                clock = new Timer();
                clock.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (activeService == null || activeService.finalTime == null)
                            return;
                        String display;
                        long diff = Service.getDifferenceTimeInMillis(activeService.finalTime);
                        int minutes = (int)diff/1000/60 + 1;
                        if (diff < 0) {
                            display = getResources().getString(R.string.started) + " 0 min";
                        }
                        else {
                            display = getResources().getString(R.string.started) + " " + minutes + " min";
                        }
                        configureActiveService(display);
                    }
                },1,1000);
                break;
            case "Finished":
                configureActiveServiceForFinished();
                break;
        }
        AppData.notifyNewData(settings,false);
    }

    private void configureActiveServiceForLooking() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                serviceInfo.setText(R.string.looking);
                cancelButton.setVisibility(View.VISIBLE);
                if (activeService != null)
                    centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                for (int i = 0; i < markers.size(); i++) {
                    Marker marker = markers.get(i);
                    marker.remove();
                }
            }
        });
    }

    private void configureActiveService(final String display) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activeService == null)
                    return;
                cleanerInfo.setVisibility(View.VISIBLE);
                cleanerImageInfo.setVisibility(View.VISIBLE);
                cleanerInfo.setText(activeService.cleanerName);
                serviceInfo.setText(display);
                centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
            }
        });
        setImageDrawableForActiveService();
    }

    private void configureActiveServiceForFinished() {
        if (clock != null) clock.cancel();
        if (activeService.rating == -1)
            changeActivity(SummaryActivity.class);
        serviceRequestedFlag = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                cleanerInfo.setText(getString(R.string.init_string));
                cleanerImageInfo.setImageDrawable(null);
                serviceInfo.setText(getString(R.string.looking));
            }
        });
    }

    private void configureServiceForDelete() {
        serviceRequestedFlag = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleanerInfo.setVisibility(View.GONE);
                cleanerImageInfo.setVisibility(View.GONE);
                cleanerInfo.setText(getString(R.string.init_string));
                cleanerImageInfo.setImageDrawable(null);
                serviceInfo.setText(getString(R.string.init_string));
                onResume();
            }
        });
    }


    private void checkNotification() {
        final String message = settings.getString(AppData.MESSAGE,null);
        if (message != null && !message.equals("Finished")) {
            AppData.deleteMessage(settings);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(message);
                }
            });
        }
    }

    private void buildAlertForCancel(){
        if (cancelCode == 2) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.service_taking_long))
                    .setMessage(getString(R.string.service_taking_long_message))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sendCancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                        }
                    })
                    .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                            cancelAlarmClock = new Timer();
                            cancelAlarmClock.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildAlertForCancel();
                                            if (settings.getBoolean(AppData.IN_BACKGROUND, false))
                                                ServiceStatusNotification.notify(getBaseContext(), getString(R.string.wish_cancel), NavigationDrawer.class);
                                        }
                                    });
                                }
                            }, ONE_SECOND * 60 * 15);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel))
                    .setMessage(getString(R.string.cancel_with_charge))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sendCancel();
                            if (cancelAlarmClock != null) cancelAlarmClock.cancel();
                        }
                    })
                    .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }


    public void setImageDrawableForActiveService() {
        try {
            URL url = new URL("http://imanio.zone/Vashen/images/cleaners/" + activeService.cleanerId + "/profile_image.jpg");
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            if (bm == null)
                return;

            final Drawable image = new BitmapDrawable(getResources(), bm);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cleanerImageInfo.setImageDrawable(image);
                }
            });

        } catch (Exception e) {
            Log.i("Error","");
        }
    }


    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        idClient = settings.getString(AppData.IDCLIENTE, null);
        token = settings.getString(AppData.TOKEN,null);
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
        creditCard = db.readCard();
        activeService = db.getActiveService();
        if (activeService == null) {
            viewState = STANDBY;
            configureState();
        } else if (activeService.status.equals("Finished")) {
            changeActivity(SummaryActivity.class);
            viewState = SERVICE_START;
            configureState();
        }
        else {
            viewState = SERVICE_START;
            configureState();
            startActiveServiceCycle();
        }
    }

    private void initView() {
        configureMenu();
        configureActionBar();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        vehiclesButton = (ImageView) findViewById(R.id.vehiclesImage);
        startLayout = (LinearLayout) findViewById(R.id.startLayout);
        lowLayout = (LinearLayout) findViewById(R.id.lowLayout);
        upLayout = (LinearLayout) findViewById(R.id.upLayout);
        rightLayout = (LinearLayout) findViewById(R.id.rightLayout);
        leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
        leftButton = (TextView) findViewById(R.id.leftButton);
        leftImage = (ImageView) findViewById(R.id.leftButtonImage);
        leftDescription = (TextView) findViewById(R.id.leftDescription);
        rightButton = (TextView) findViewById(R.id.rightButton);
        rightImage = (ImageView) findViewById(R.id.rightButtonImage);
        rightDescription = (TextView) findViewById(R.id.rightDescription);
        cleanerInfo = (TextView) findViewById(R.id.cleanerInfo);
        cleanerImageInfo = (ImageView) findViewById(R.id.cleanerImageInfo);
        serviceInfo = (TextView) findViewById(R.id.serviceInfo);
        serviceLocationText = (EditText) findViewById(R.id.serviceLocationText);
        locationText = (TextView) findViewById(R.id.locationText);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        serviceLocationText.setOnEditorActionListener(this);
        startLayout.setOnClickListener(this);
    }

    private void readUserImage() {
        TextView headerTitle = (TextView) findViewById(R.id.menuName);
        ImageView menuImage = (ImageView) findViewById(R.id.menuImage);
        headerTitle.setText(getString(R.string.user_name,user.name,user.lastName));
        menuImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
    }

    private void configureServices() {
        vehiclesButton.setAlpha(0.5f);
        vehiclesButton.setClickable(false);
        int type = 6;
        Car selectedCar = new DataBase(getBaseContext()).getFavoriteCar();
        if (selectedCar != null)
            type = Integer.valueOf(selectedCar.type);

        switch (type) {
            case Service.BIKE:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.bike_active));
                break;
            case Service.CAR:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.car_active));
                break;
            case Service.SMALL_VAN:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.small_van_active));
                break;
            case Service.BIG_VAN:
                vehiclesButton.setAlpha(1.0f);
                vehiclesButton.setClickable(true);
                vehiclesButton.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.big_van_active));
                break;
            default:
                break;
        }
    }


    private void initLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        } catch (SecurityException e) {
            postAlert("Location services not working");
        }
    }

    private void initTimers() {
        nearbyCleanersTimer = new Timer();
        nearbyCleanersTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (activeService == null) {
                    try {
                        getNearbyCleaners();
                    } catch (Cleaner.noSessionFound e) {
                        if (!MainActivity.onScreen && settings.getString(AppData.TOKEN,null) != null) {
                            postAlert(getString(R.string.session_error));
                            changeActivity(MainActivity.class);
                        }
                        cancel();
                        cancelTimers();
                        finish();
                    }
                }
            }
        }, 0, ONE_SECOND / 50);
        reloadMapTimer = new Timer();
        reloadMapTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                reloadCleanerMarkers();
            }
        }, 0, ONE_SECOND / 50);
        reloadAddressTimer = new Timer();
        reloadAddressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getGeoLocation();
                getUserGeoLocaton();
            }
        }, 0, ONE_SECOND);
    }


    private void cancelTimers() {
        if (nearbyCleanersTimer != null) {
            nearbyCleanersTimer.cancel();
        }
        if (reloadMapTimer != null) {
            reloadMapTimer.cancel();
        }
        if (reloadAddressTimer != null) {
            reloadAddressTimer.cancel();
        }
    }

    public void sendRequestService(){
        try {
            Car favCar = new DataBase(getBaseContext()).getFavoriteCar();
            Service serviceRequested = Service.requestService("", String.valueOf(requestLocation.latitude),
                    String.valueOf(requestLocation.longitude), service, serviceType, token, vehicleType, favCar.id);
            DataBase db = new DataBase(getBaseContext());
            List<Service> services = db.readServices();
            services.add(serviceRequested);
            db.saveServices(services);
            cancelCode = 0;
            activeService = serviceRequested;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    upLayout.setVisibility(LinearLayout.GONE);
                    lowLayout.setVisibility(LinearLayout.GONE);
                    startLayout.setVisibility(LinearLayout.VISIBLE);
                    cancelButton .setVisibility(View.VISIBLE);
                    cleanerInfo.setVisibility(View.GONE);
                    serviceLocationText.setEnabled(true);
                    serviceInfo.setText(R.string.looking);
                    requestingAlert.cancel();
                }
            });
            startActiveServiceCycle();
            cancelSent = false;
        } catch (Service.errorRequestingService e) {
            postAlert("Error requesting service");
            serviceRequestedFlag = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onResume();
                    requestingAlert.cancel();
                }
            });
        } catch (Service.noSessionFound e){
            if (!MainActivity.onScreen && settings.getString(AppData.TOKEN,null) != null)  {
                postAlert(getString(R.string.session_error));
                changeActivity(MainActivity.class);
            }
            cancelTimers();
            finish();
        } catch (Service.userBlock e){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(getString(R.string.user_block));
                    serviceRequestedFlag = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResume();
                            requestingAlert.cancel();
                        }
                    });
                }
            });
        }
    }

    private void getNearbyCleaners() throws Cleaner.noSessionFound {
        try {
            if (requestLocation != null)
                cleaners = Cleaner.getNearbyCleaners(requestLocation.latitude, requestLocation.longitude,token);
        } catch (Cleaner.errorGettingCleaners e){
            Log.i("Cleaners Error","Couldnt retrieve cleaners try again later");
        }
    }

    private void reloadCleanerMarkers() {
        try{
            if (activeService != null && !activeService.status.equals("Looking"))
                cleaner = Cleaner.getCleanerLocation(activeService.cleanerId,token);
        } catch (Cleaner.errorGettingCleaners e){
            Log.i("ERROR","Reading cleaner location");
        }  catch (Cleaner.noSessionFound e){
            if (!MainActivity.onScreen && settings.getString(AppData.TOKEN,null) != null) {
                postAlert(getString(R.string.session_error));
                changeActivity(MainActivity.class);
            }
            cancelTimers();
            finish();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (activeService != null) {
                    centralMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                    if (!activeService.status.equals("Looking") && cleaner != null) {
                        cleanerMarker.setVisible(true);
                        cleanerMarker.setPosition(new LatLng(cleaner.latitud, cleaner.longitud));
                    }
                } else {
                    cleanerMarker.setVisible(false);
                    requestLocation = map.getCameraPosition().target;
                    centralMarker.setPosition(new LatLng(requestLocation.latitude, requestLocation.longitude));
                    if (cleaners.size() >= markers.size())
                        addMarkersAndUpdate();
                    else
                        removeMarkersAndUpdate();
                }
            }
        });
    }

    private void removeMarkersAndUpdate() {
        List<Marker> aux = new ArrayList<>();

        for (int i = 0; i < cleaners.size();i++){
            aux.add(markers.get(i));
            Cleaner cleaner = cleaners.get(i);
            aux.get(i).setPosition(new LatLng(cleaner.latitud,cleaner.longitud));
        }
        for (int i = cleaners.size(); i < markers.size(); i++) {
            Marker marker = markers.get(i);
            marker.remove();
        }
        markers = aux;
    }

    private void addMarkersAndUpdate() {
        List<Marker> aux = new ArrayList<>();
        LatLng auxLocation = new LatLng(0.0,0.0);
        BitmapDescriptor cleanerImage = BitmapDescriptorFactory.fromResource(R.drawable.washer);
        for (int i = 0; i < cleaners.size();i++){
            if (i < markers.size())
                aux.add(markers.get(i));
            else
                aux.add(map.addMarker(new MarkerOptions()
                        .position(auxLocation)
                        .icon(cleanerImage)));
            Cleaner cleaner = cleaners.get(i);
            aux.get(i).setPosition(new LatLng(cleaner.latitud,cleaner.longitud));
        }
        markers = aux;
    }

    private void getGeoLocation() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(requestLocation.latitude, requestLocation.longitude, 1);
            if (addresses.size() < 1)
                return;
            final String address = addresses.get(0).getAddressLine(0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (serviceLocationText.didTouchFocusSelect())
                        serviceLocationText.setText(address);
                }
            });
        } catch (Throwable e) {
            Log.i("LOCATION","Error gettin geo location = " + e.getMessage());
        }
    }

    private void getUserGeoLocaton() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(crit,true);
            Location lastLocation = locationManager.getLastKnownLocation(provider);
            addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            if (addresses.size() < 1)
                return;
            final String address = addresses.get(0).getAddressLine(0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                        locationText.setText(address);
                }
            });
        } catch (SecurityException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(getString(R.string.no_location_service));
                }
            });
        } catch (Throwable e) {
            Log.i("LOCATION","Error gettin geo location = " + e.getMessage());
        }
    }

    public void vehicleClicked(View view) {
        if (viewState != STANDBY)
            return;
        if (creditCard.cardNumber == null) {
            postAlert(getString(R.string.no_credit_card));
            return;
        }
        if (cleaners.size() < 1) {
            postAlert(getString(R.string.no_cleaners));
            return;
        }
        //Always eco
        //viewState = VEHICLE_SELECTED;
        serviceType = String.valueOf(Service.ECO);
        viewState = ECO_OR_TRADITIONAL_SELECTED;
        vehicleType = new DataBase(getBaseContext()).getFavoriteCar().type;
        configureState();
    }

    private void configureState() {
        switch (viewState) {
            case STANDBY:
                configureStandbyState();
                break;
            case VEHICLE_SELECTED:
                configureVehicleSelectedState();
                break;
            case ECO_OR_TRADITIONAL_SELECTED:
                configureServiceSelectedState();
                break;
            case OUTSIDE_OR_INSIDE_SELECTED:
                configureServiceTypeState();
                viewState = SERVICE_START;
                break;
            case SERVICE_START:
                configureServiceStartState();
                viewState = -1;
                break;
        }
    }

    private void configureStandbyState() {
        upLayout.setVisibility(LinearLayout.VISIBLE);
        lowLayout.setVisibility(LinearLayout.GONE);
        startLayout.setVisibility(LinearLayout.GONE);
        serviceLocationText.setVisibility(View.VISIBLE);
        locationText.setVisibility(View.VISIBLE);
    }

    private void configureVehicleSelectedState() {
        upLayout.setVisibility(LinearLayout.VISIBLE);
        lowLayout.setVisibility(LinearLayout.VISIBLE);
        startLayout.setVisibility(LinearLayout.GONE);
        serviceLocationText.setVisibility(View.GONE);
        locationText.setVisibility(View.GONE);
        leftButton.setText(R.string.eco);
        leftImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.ecologico));
        leftDescription.setText(R.string.eco_description);
        rightButton.setText(R.string.traditional);
        rightImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.tradicional));
        rightDescription.setText(R.string.traditional_description);
        serviceLocationText.setEnabled(true);
    }

    private void configureServiceSelectedState() {
        upLayout.setVisibility(LinearLayout.VISIBLE);
        lowLayout.setVisibility(LinearLayout.VISIBLE);
        startLayout.setVisibility(LinearLayout.GONE);
        serviceLocationText.setVisibility(View.GONE);
        locationText.setVisibility(View.GONE);
        if (vehicleType.equals(String.valueOf(Service.BIKE))) {
            rightLayout.setVisibility(View.INVISIBLE);
        } else {
            rightLayout.setVisibility(View.VISIBLE);
        }
        leftButton.setText(R.string.outside);
        leftImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.exterior));
        leftDescription.setText(R.string.outside_description);
        rightButton.setText(R.string.outside_and_inside);
        rightImage.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),R.drawable.interior));
        rightDescription.setText(R.string.outside_and_inside_description);
        serviceLocationText.setEnabled(true);
    }

    private void configureServiceTypeState() {
        createAlertRequesting();
        Thread sendRequestServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (serviceRequestedFlag)
                    return;
                serviceRequestedFlag = true;
                sendRequestService();
            }
        });
        sendRequestServiceThread.start();
    }

    private void configureServiceStartState() {
        upLayout.setVisibility(LinearLayout.GONE);
        lowLayout.setVisibility(LinearLayout.GONE);
        startLayout.setVisibility(LinearLayout.VISIBLE);
        serviceLocationText.setVisibility(View.GONE);
        locationText.setVisibility(View.GONE);
        cancelButton.setVisibility(View.VISIBLE);
        serviceInfo.setText(R.string.looking);
        serviceLocationText.setEnabled(false);
        configureActiveServiceView();
    }

    public void leftClick(View view) {
        if (viewState == VEHICLE_SELECTED) {
            serviceType = String.valueOf(Service.ECO);
            viewState = ECO_OR_TRADITIONAL_SELECTED;
        }
        else {
            service = String.valueOf(Service.OUTSIDE);
            viewState = OUTSIDE_OR_INSIDE_SELECTED;
        }
        configureState();
    }

    public void rightClick(View view) {
        if (viewState == VEHICLE_SELECTED) {
            serviceType = String.valueOf(Service.TRADITIONAL);
            viewState = ECO_OR_TRADITIONAL_SELECTED;
        }
        else {
            service = String.valueOf(Service.OUTSIDE_INSIDE);
            viewState = OUTSIDE_OR_INSIDE_SELECTED;
        }
        configureState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            BitmapDescriptor cleanerImage = BitmapDescriptorFactory.fromResource(R.drawable.washer);
            map = googleMap;
            map.setMyLocationEnabled(true);
            map.setPadding(0,0,0,0);
            //map.setTrafficEnabled(true);
            //map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            cleanerMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(0,0))
                    .icon(cleanerImage));
            cleanerMarker.setVisible(false);
            Location lastKnownLocation;
            if ((lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) == null)
                if ((lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) == null) {
                    centralMarker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(0,0))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    createAlert(getString(R.string.no_location_service));
                    return;
                }
            requestLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(requestLocation,15);
            map.moveCamera(cameraUpdate);
            locationManager.removeUpdates(this);
            centralMarker = map.addMarker(new MarkerOptions()
                    .position(requestLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } catch (SecurityException e) {
            postAlert("Error loading map");
        }
    }

    private void createAlert(String title) {
        new AlertDialog.Builder(this)
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createAlertRequesting( ) {
        requestingAlert = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.requesting))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_map);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuTitle = (TextView)findViewById(R.id.menuMapTitle);
        menuTitle.setText(R.string.app_name_display);
        menuTitle.setTextColor(Color.rgb(6,140,135));
    }

    private void configureMenu() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ListView menuList = (ListView)findViewById(R.id.menuList);
        View header = getLayoutInflater().inflate(R.layout.menu_header,menuList,false);
        menuList.addHeaderView(header);
        String [] titles = getResources().getStringArray(R.array.menu_options);
        Collections.addAll(navigationItems, titles);
        listItems.add(Pair.create(titles[0], ContextCompat.getDrawable(getBaseContext(),R.drawable.pay_icon)));
        listItems.add(Pair.create(titles[1], ContextCompat.getDrawable(getBaseContext(),R.drawable.bill_icon)));
        listItems.add(Pair.create(titles[2], ContextCompat.getDrawable(getBaseContext(),R.drawable.hist_icon)));
        listItems.add(Pair.create(titles[3], ContextCompat.getDrawable(getBaseContext(),R.drawable.car_icon)));
        listItems.add(Pair.create(titles[4], ContextCompat.getDrawable(getBaseContext(),R.drawable.help_icon)));
        listItems.add(Pair.create(titles[5], ContextCompat.getDrawable(getBaseContext(),R.drawable.work_icon)));
        listItems.add(Pair.create(titles[6], ContextCompat.getDrawable(getBaseContext(),R.drawable.config_icon)));
        listItems.add(Pair.create(titles[7], ContextCompat.getDrawable(getBaseContext(),R.drawable.line_white)));
        final MenuAdapter adapter = new MenuAdapter();
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                decideFragment(position);
            }
        });
    }

    private void decideFragment(int position) {
        switch (position){
            case PAYMENT:
                changeActivity(PaymentActivity.class);
                return;
            case BILLING:
                changeActivity(BillingActivity.class);
                return;
            case HISTORY:
                changeActivity(HistoryActivity.class);
                return;
            case CARS:
                changeActivity(CarsActivity.class);
                return;
            case HELP:
                changeActivity(HelpActivity.class);
                return;
            case BE_PART_OF_TEAM:
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.washer.mx"));
                startActivity(myIntent);
                break;
            case CONFIGURATION:
                changeActivity(ConfigurationActivity.class);
                return;
            case ABOUT:
                changeActivity(AboutActivity.class);
                return;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startLayout) {
            changeActivity(InformationActivity.class);
        }
    }

    public void onClickMenu(View view){
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            drawerLayout.openDrawer(GravityCompat.START);
    }

    private void changeActivity(Class activity) {
        Intent intent = new Intent(getBaseContext(), activity);
        startActivity(intent);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (viewState == SERVICE_START || viewState == STANDBY) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            viewState = STANDBY;
            configureState();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final String location = serviceLocationText.getText().toString();
        Thread sendModifyLocationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                modifyLocation(location);
            }
        });
        sendModifyLocationThread.start();
        return true;
    }

    private void modifyLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(location,5);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(crit,true);
            Location lastLocation = locationManager.getLastKnownLocation(provider);
            if (addresses.size() < 1)
                return;
            Address closestAddress = addresses.get(0);
            float lastDistance = 0;
            for (Address address:addresses){
                Location newLocation = new Location(provider);
                newLocation.setLatitude(address.getLatitude());
                newLocation.setLongitude(address.getLongitude());
                float distance = lastLocation.distanceTo(newLocation);
                if (lastDistance > distance)
                    closestAddress = address;
                lastDistance = distance;
            }
            final LatLng locationLatLng = new LatLng(closestAddress.getLatitude(),closestAddress.getLongitude());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locationLatLng,15);
                    map.animateCamera(cameraUpdate);
                    InputMethodManager imn = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imn.hideSoftInputFromWindow(serviceLocationText.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    serviceLocationText.clearFocus();
                }
            });
        } catch (SecurityException e){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createAlert(getString(R.string.no_location_service));
                }
            });
        } catch (Exception e) {
            postAlert("Error getting street name");
        }
    }

    public void onClickCancel(View view) {
        if (cancelCode == 0)
            sendCancel();
        else if (cancelCode == 1)
            buildAlertForCancel();
    }

    private void sendCancel(){
        Thread sendCancelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cancelSent)
                        return;
                    cancelSent = true;
                    Service.cancelService(activeService.id,token,cancelCode);
                    if (cancelAlarmClock != null){
                        cancelAlarmClock.cancel();
                        cancelAlarmClock.purge();
                        cancelAlarmClock = null;
                    }
                } catch (Throwable e) {
                    Log.i("CANCEL","Error canceling");
                    postAlert(getString(R.string.error_on_cancel));
                    cancelSent = false;
                }
            }
        });
        sendCancelThread.start();
    }

    private class MenuAdapter extends ArrayAdapter<Pair<String,Drawable>> {
        MenuAdapter()
        {
            super(NavigationDrawer.this,R.layout.menu_item,R.id.listItemName,listItems);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.menu_item, parent, false);
            }
            try {
                Pair<String,Drawable> item = listItems.get(position);
                Drawable icon = item.second;
                TextView itemName = (TextView)itemView.findViewById(R.id.listItemName);
                ImageView itemImage = (ImageView)itemView.findViewById(R.id.listItemImage);
                if (position < navigationItems.size() - 1) {
                    itemImage.setImageDrawable(icon);
                } else {
                    itemName.setCompoundDrawablesWithIntrinsicBounds(null,icon,null,null);
                    itemImage.setImageDrawable(null);
                }
                itemName.setText(item.first);
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {  }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }
}
