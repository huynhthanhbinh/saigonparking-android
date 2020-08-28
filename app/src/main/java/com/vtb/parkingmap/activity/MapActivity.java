package com.vtb.parkingmap.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotResult;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotServiceGrpc;
import com.bht.saigonparking.api.grpc.parkinglot.ScanningByRadiusRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.SphericalUtil;
import com.google.protobuf.Int64Value;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mancj.slideup.SlideUp;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.models.MyPlaces;
import com.vtb.parkingmap.models.Results;
import com.vtb.parkingmap.remotes.GoogleApiService;
import com.vtb.parkingmap.support.MapActivitySupport;
import com.vtb.parkingmap.support.ParkingListAdapter;
import com.vtb.parkingmap.support.TouchableWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.ghyeok.stickyswitch.widget.StickySwitch;
import io.grpc.StatusRuntimeException;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Getter
@Setter
@SuppressLint("all")
@SuppressWarnings("all")
public final class MapActivity extends BaseSaigonParkingActivity implements OnMapReadyCallback, TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp, View.OnKeyListener, NavigationView.OnNavigationItemSelectedListener {

    private MapActivitySupport mapActivitySupport;
    private ParkingLotServiceGrpc.ParkingLotServiceBlockingStub parkingLotServiceBlockingStub;

    //biến lưu vị trí khi touch màn hình trước đó
    LatLng preNorthEast = null;
    LatLng preSouthWest = null;
    // độ zoom


    private float currentZoom = -1;

    SparseIntArray zoomRadiusMap;
    SparseIntArray zoomNResultMap;

    // model
    private MyPlaces myPlaces;
    private Context context;
    //
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    GoogleApiService mGoogleApiService;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;


    private Location mLastKnownLocation;
    private Marker marker;
    private double latitude, longitude;
    private LatLng position3;

    //navigation menu panel
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private ImageButton btnAbout;
    private ImageButton flagdrawdirect;
    private final int RECOGNIZER_REQ_CODE = 100; //code activity result speech


    //other locations
    private SlideUp slideUp;
    private View dim;
    private View slideView;
    private ImageButton fab;
    private ImageButton bnt_mylocation;
    private CardView imgbtnrestaurant;
    private CardView imgbtnhospital;
    private CardView imgbtnGasStation;
    private CardView imgbtnParkinglot;
    private boolean modeParkingLot = true;
    private String snipFirstClick = "";
    private LottieAnimationView searchAnimation;
    private LottieAnimationView searchAnimationonMove;
    private ImageView profileLottie;
    private ImageView saigonParking;
    private LinearLayout lnHeader;

    private final float DEFAULT_ZOOM = 14;
    // xử lý sự kiện màn hình
    CameraPosition mUpCameraPosition;
    //Xử lý phần API
    // List
    Set<ParkingLotResult> parkingLotResultSet;
    List<ParkingLotResult> recommendedParkingLotResultList;
    int option = 1;

    // biến thông tin bãi xe
    ParkingLot parkingLot;
    // chế độ gửi xe
    int flatdrawdirection = 0;
    int flatfunction = 1; // = 0 là ở dạng bình thường , = 1 là  ở dạng không cho kéo màn hình
    int flatscreen = 0;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapActivitySupport = new MapActivitySupport(this, (SaigonParkingApplication) getApplicationContext());

        parkingLotServiceBlockingStub = serviceStubs.getParkingLotServiceBlockingStub();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_map);
        parkingLotResultSet = new HashSet<>();
        recommendedParkingLotResultList = new ArrayList<>();
        ListView listView = findViewById(R.id.listview);

        //độ zoom
        zoomRadiusMap = new SparseIntArray(4);
        zoomRadiusMap.put(14, 3);
        zoomRadiusMap.put(15, 3);
        zoomRadiusMap.put(16, 2);
        zoomRadiusMap.put(17, 1);

        zoomNResultMap = new SparseIntArray(4);
        zoomNResultMap.put(14, 10);
        zoomNResultMap.put(15, 8);
        zoomNResultMap.put(16, 5);
        zoomNResultMap.put(17, 3);
        //

        //navigation menu panel
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btn_find);
        searchAnimation = findViewById(R.id.searchAnimation);
        searchAnimationonMove = findViewById(R.id.searchAnimationOnMove);

        profileLottie = navigationView.getHeaderView(0).findViewById(R.id.profileLottie);
        saigonParking = navigationView.getHeaderView(0).findViewById(R.id.saigonParking);
        lnHeader = navigationView.getHeaderView(0).findViewById(R.id.lnHeader);
        @Nullable Intent data;


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        //Switch Find destination
        StickySwitch stickySwitchDestination = (StickySwitch) findViewById(R.id.sticky_switch_destination);
        stickySwitchDestination.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NonNull StickySwitch.Direction direction, @NonNull String text) {
                if (stickySwitchDestination.getDirection() == StickySwitch.Direction.LEFT) {
                    Toast.makeText(MapActivity.this, "MODE OFF : Find Destination", Toast.LENGTH_SHORT).show();
                    flatdrawdirection = 0;
                    position3 = null;
                } else {
                    Toast.makeText(MapActivity.this, "MODE ON : Find Destination", Toast.LENGTH_SHORT).show();
                    flatdrawdirection = 1;
                }
            }
        });

        //Switch draglocation
        StickySwitch stickySwitchDragLocation = (StickySwitch) findViewById(R.id.sticky_switch_draglocation);
        stickySwitchDragLocation.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NonNull StickySwitch.Direction direction, @NonNull String text) {
                if (stickySwitchDragLocation.getDirection() == StickySwitch.Direction.LEFT) {
                    Toast.makeText(MapActivity.this, "Standard MODE ON", Toast.LENGTH_SHORT).show();
                    if (mMap != null) {
                        mMap.clear();
                    }
                    if (recommendedParkingLotResultList != null) {
                        recommendedParkingLotResultList.clear();
                    }
                    flatfunction = 1;
                } else {
                    Toast.makeText(MapActivity.this, "Optimal MODE ON", Toast.LENGTH_SHORT).show();
                    if (mMap != null) {
                        mMap.clear();
                    }
                    if (recommendedParkingLotResultList != null) {
                        recommendedParkingLotResultList.clear();
                    }
                    flatfunction = 0;
                    parkingLotResultSet.clear();
                }
            }
        });

        //google api service
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ParkingLotResult selectedFromList = (ParkingLotResult) listView.getItemAtPosition(i);

                callApiWithExceptionHandling(() -> {
                    parkingLot = parkingLotServiceBlockingStub
                            .getParkingLotById(Int64Value.newBuilder()
                                    .setValue(selectedFromList.getId())
                                    .build());
                });


                Log.d("khongbiloi", "" + Long.parseLong(marker.getSnippet()));
                Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
                intent.putExtra("parkingLot", (Serializable) parkingLot);
                intent.putExtra("myLat", (Serializable) mLastKnownLocation.getLatitude());
                intent.putExtra("myLong", (Serializable) mLastKnownLocation.getLongitude());
                if (position3 != null) {
                    intent.putExtra("postion3lat", position3.latitude);
                    intent.putExtra("postion3long", position3.longitude);
                }
                startActivity(intent);


//                Toast.makeText(MapActivity.this, "" + selectedFromList, Toast.LENGTH_SHORT).show();

            }
        });
        mGoogleApiService = googleApiService;

        //Create place api
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this, getString(R.string.google_maps_api));
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Action when search
        materialSearchBar.setSpeechMode(true);
        materialSearchBar.isSpeechModeEnabled();
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                materialSearchBar.clearSuggestions();
                materialSearchBar.hideSuggestionsList();
            }

            //Event button Enter when search
            @Override
            public void onSearchConfirmed(CharSequence text) {
                if (materialSearchBar.isSuggestionsVisible() == true && predictionList.size() > 0) {
                    AutocompletePrediction selectedPrediction = predictionList.get(0);
                    String suggestion = materialSearchBar.getLastSuggestions().get(0).toString();
                    materialSearchBar.setText(suggestion);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            materialSearchBar.clearSuggestions();
                            materialSearchBar.hideSuggestionsList();
                        }
                    }, 1000);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    String placeId = selectedPrediction.getPlaceId();
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                    FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            Place place = fetchPlaceResponse.getPlace();
                            Log.i("mytag", "Place found: " + place.getName());
                            LatLng latLngOfPlace = place.getLatLng();
                            if (latLngOfPlace != null) {


                                if (position3 != null) {
                                    position3 = null;
                                } else {
                                    if (flatdrawdirection == 0) {
                                        position3 = null;
                                    } else {
                                        position3 = latLngOfPlace;
                                    }
                                }
                                Log.d("Bach", "Tim kiem" + latLngOfPlace.toString());
                                // tim kiem dia diem noi den
                                if (recommendedParkingLotResultList != null) {
                                    recommendedParkingLotResultList.clear();

                                }
                                if (mMap != null) {
                                    mMap.clear();
                                }
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                                MarkerOptions tmpMaker = new MarkerOptions()
                                        .position(new LatLng(latLngOfPlace.latitude, latLngOfPlace.longitude))
                                        .title("Location you Find")

                                        .icon(BitmapDescriptorFactory.defaultMarker());
                                mMap.addMarker(tmpMaker).showInfoWindow();
                                if (option == 1) {
                                    try {
                                        Objects.requireNonNull(recommendedParkingLotResultList)
                                                .addAll(parkingLotServiceBlockingStub
                                                        .getTopParkingLotInRegionOrderByDistanceWithName(ScanningByRadiusRequest.newBuilder()
                                                                .setLatitude(latLngOfPlace.latitude)
                                                                .setLongitude(latLngOfPlace.longitude)
                                                                .setRadiusToScan(3)
                                                                .setNResult(10)
                                                                .build())
                                                        .getParkingLotResultList());

                                        setAllMarkerParkingLot(recommendedParkingLotResultList);

                                        List<ParkingLotResult> a = new ArrayList<>(recommendedParkingLotResultList);
                                        for (int i = 0; i < a.size(); i++) {
                                            Double distance = SphericalUtil.computeDistanceBetween(
                                                    new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                                    new LatLng(a.get(i).getLatitude(), a.get(i).getLongitude()));

                                            ParkingLotResult parkingLotResult = a.get(i).toBuilder()
                                                    .setDistance(distance / 1000)
                                                    .build();
                                            a.set(i, parkingLotResult);
                                        }

                                        ParkingListAdapter adapter = new ParkingListAdapter(MapActivity.this,
                                                R.layout.adapter_view_layout, mapActivitySupport.sortParkingLotResultList(a));
                                        listView.setAdapter(adapter);

                                    } catch (StatusRuntimeException exception) {
                                        saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
                                    } catch (Exception e) {
                                        Toast.makeText(MapActivity.this, "Co loi khi load ve Server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
//                                if (option == 2) {
//                                    nearbyPlaces("restaurant");
//
//                                }
//                                if (option == 3) {
//                                    nearbyPlaces("hospital");
//
//                                }
//                                if (option == 4) {
//                                    nearbyPlaces("gas station");
//
//                                }
                            }
                        }
                    }).addOnFailureListener(e -> {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    });
                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    drawer.openDrawer(GravityCompat.START);
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                    materialSearchBar.clearSuggestions();
                    materialSearchBar.hideSuggestionsList();

                } else if (buttonCode == MaterialSearchBar.BUTTON_SPEECH) {
                    Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                    try {
                        startActivityForResult(voiceIntent, RECOGNIZER_REQ_CODE);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.speech_not_supported),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //event text change when search

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("VN")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    position3 = null;
                }

            }
        });

        //Event click one optional on suggestion when search
        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                        materialSearchBar.hideSuggestionsList();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {


                            if (position3 != null) {
                                position3 = null;
                            } else {
                                if (flatdrawdirection == 0) {
                                    position3 = null;
                                } else {
                                    position3 = latLngOfPlace;
                                }
                            }


                            Log.d("Bach", "Tim kiem" + latLngOfPlace.toString());

                            // tim kiem dia diem noi den
                            if (recommendedParkingLotResultList != null) {
                                recommendedParkingLotResultList.clear();

                            }
                            if (mMap != null) {
                                mMap.clear();
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                            MarkerOptions tmpMaker = new MarkerOptions()
                                    .position(new LatLng(latLngOfPlace.latitude, latLngOfPlace.longitude))
                                    .title("Location you Find")

                                    .icon(BitmapDescriptorFactory.defaultMarker());
                            mMap.addMarker(tmpMaker).showInfoWindow();
                            if (option == 1) {
                                try {
                                    Objects.requireNonNull(recommendedParkingLotResultList)
                                            .addAll(parkingLotServiceBlockingStub
                                                    .getTopParkingLotInRegionOrderByDistanceWithName(ScanningByRadiusRequest.newBuilder()
                                                            .setLatitude(latLngOfPlace.latitude)
                                                            .setLongitude(latLngOfPlace.longitude)
                                                            .setRadiusToScan(3)
                                                            .setNResult(10)
                                                            .build())
                                                    .getParkingLotResultList());

                                    setAllMarkerParkingLot(recommendedParkingLotResultList);

                                    List<ParkingLotResult> parkingLotResultList = new ArrayList<>(recommendedParkingLotResultList);
                                    for (int i = 0; i < parkingLotResultList.size(); i++) {
                                        Double distance = SphericalUtil.computeDistanceBetween(
                                                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                                new LatLng(parkingLotResultList.get(i).getLatitude(), parkingLotResultList.get(i).getLongitude()));

                                        ParkingLotResult parkingLotResult = parkingLotResultList.get(i).toBuilder()
                                                .setDistance(distance / 1000)
                                                .build();
                                        parkingLotResultList.set(i, parkingLotResult);
                                    }
                                    ParkingListAdapter adapter = new ParkingListAdapter(MapActivity.this,
                                            R.layout.adapter_view_layout, mapActivitySupport.sortParkingLotResultList(parkingLotResultList));
                                    listView.setAdapter(adapter);
                                } catch (StatusRuntimeException exception) {
                                    saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
                                } catch (Exception e) {
                                    Toast.makeText(MapActivity.this, "Co loi khi load ve Server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
//                            if (option == 2) {
//                                nearbyPlaces("restaurant");
//                            }
                        }
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        apiException.printStackTrace();
                        int statusCode = apiException.getStatusCode();
                        Log.i("mytag", "place not found: " + e.getMessage());
                        Log.i("mytag", "status code: " + statusCode);
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        //Event click button find parkinglot
        btnFind.setOnClickListener(v -> {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM), 500, null);
            modeParkingLot = true;
            LatLng currentMarkerLocation = mMap.getCameraPosition().target;
            //Xoa cu
            if (recommendedParkingLotResultList != null) {
                recommendedParkingLotResultList.clear();

            }
            if (mMap != null) {
                mMap.clear();
            }
            searchAnimation.loop(true);
            searchAnimation.playAnimation();

            try {

                Objects.requireNonNull(recommendedParkingLotResultList)
                        .addAll(parkingLotServiceBlockingStub
                                .getTopParkingLotInRegionOrderByDistanceWithName(ScanningByRadiusRequest.newBuilder()
                                        .setLatitude(mLastKnownLocation.getLatitude())
                                        .setLongitude(mLastKnownLocation.getLongitude())
                                        .setRadiusToScan(3)
                                        .setNResult(10)
                                        .build())
                                .getParkingLotResultList());

                setAllMarkerParkingLot(recommendedParkingLotResultList);

                List<ParkingLotResult> parkingLotResultList = new ArrayList<>(recommendedParkingLotResultList);
                for (int i = 0; i < parkingLotResultList.size(); i++) {
                    Double distance = SphericalUtil.computeDistanceBetween(
                            new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                            new LatLng(parkingLotResultList.get(i).getLatitude(), parkingLotResultList.get(i).getLongitude()));

                    ParkingLotResult parkingLot = parkingLotResultList.get(i).toBuilder()
                            .setDistance(distance / 1000)
                            .build();

                    parkingLotResultList.set(i, parkingLot);
                }

                ParkingListAdapter adapter = new ParkingListAdapter(MapActivity.this,
                        R.layout.adapter_view_layout, mapActivitySupport.sortParkingLotResultList(parkingLotResultList));
                listView.setAdapter(adapter);

            } catch (StatusRuntimeException exception) {
                saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
            } catch (Exception e) {
                Toast.makeText(MapActivity.this, "Co loi khi load ve Server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            searchAnimation.loop(false);
        });


        //Tạo nút other locations
        slideView = findViewById(R.id.slideView);
        dim = findViewById(R.id.dim);

        slideUp = new SlideUp(slideView);
        slideUp.hideImmediately();

        fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideUp.animateIn();
//                fab.hide();
            }
        });

        bnt_mylocation = (ImageButton) findViewById(R.id.bnt_mylocation);
        bnt_mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
        });
        imgbtnrestaurant = findViewById(R.id.imgrestaurant);
        imgbtnrestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeParkingLot = false;
                nearbyPlaces("restaurant");
                slideUp.animateOut();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
        });


        imgbtnhospital = findViewById(R.id.imghospital);
        imgbtnhospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeParkingLot = false;
                nearbyPlaces("hospital");
                slideUp.animateOut();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
        });


        imgbtnGasStation = findViewById(R.id.imgGasStation);
        imgbtnGasStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeParkingLot = false;
                nearbyPlaces("gas_station");
                slideUp.animateOut();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
        });


        imgbtnParkinglot = findViewById(R.id.imgparkinglot);
        imgbtnParkinglot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeParkingLot = true;
                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
                //Xoa cu
                if (recommendedParkingLotResultList != null) {
                    recommendedParkingLotResultList.clear();

                }
                if (mMap != null) {
                    mMap.clear();
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                try {

                    Objects.requireNonNull(recommendedParkingLotResultList)
                            .addAll(parkingLotServiceBlockingStub
                                    .getTopParkingLotInRegionOrderByDistanceWithName(ScanningByRadiusRequest.newBuilder()
                                            .setLatitude(mLastKnownLocation.getLatitude())
                                            .setLongitude(mLastKnownLocation.getLongitude())
                                            .setRadiusToScan(3)
                                            .setNResult(10)
                                            .build())
                                    .getParkingLotResultList());

                    setAllMarkerParkingLot(recommendedParkingLotResultList);

                    List<ParkingLotResult> parkingLotResultList = new ArrayList<>(recommendedParkingLotResultList);
                    for (int i = 0; i < parkingLotResultList.size(); i++) {
                        Double distance = SphericalUtil.computeDistanceBetween(
                                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                new LatLng(parkingLotResultList.get(i).getLatitude(), parkingLotResultList.get(i).getLongitude()));

                        ParkingLotResult parkingLot = parkingLotResultList.get(i).toBuilder()
                                .setDistance(distance / 1000)
                                .build();

                        parkingLotResultList.set(i, parkingLot);
                    }


                    ParkingListAdapter adapter = new ParkingListAdapter(MapActivity.this,
                            R.layout.adapter_view_layout, mapActivitySupport.sortParkingLotResultList(parkingLotResultList));
                    listView.setAdapter(adapter);


                } catch (StatusRuntimeException exception) {
                    saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
                } catch (Exception e) {

                    Toast.makeText(MapActivity.this, "Co loi khi load ve Server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                slideUp.animateOut();

            }
        });


        slideUp.setSlideListener(new SlideUp.SlideListener() {
            @Override
            public void onSlideDown(float v) {
                dim.setAlpha(1 - (v / 100));
            }

            @Override
            public void onVisibilityChanged(int i) {
                if (i == View.GONE) {
//                    fab.show();
                }

            }
        });

        mapActivitySupport.checkCustomerHasOnGoingBooking();
    }

    void setAllMarkerParkingLot(Collection<ParkingLotResult> parkingLotResultSet) {
        parkingLotResultSet.forEach(mapActivitySupport::setMarkerParkingLot);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(14);

        if (marker != null) {
            marker.remove();
        }


        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, 180);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible()) {
                    materialSearchBar.clearSuggestions();
                }
                materialSearchBar.hideSuggestionsList();
                if (materialSearchBar.isSearchEnabled()) {
                    materialSearchBar.disableSearch();
                }
                materialSearchBar.clearSuggestions();
                materialSearchBar.hideSuggestionsList();

                return false;
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                boolean temp = false;
                if (marker.getSnippet() == null) {
                    temp = false;
                } else {
                    if (snipFirstClick.equals(marker.getSnippet())) {
                        temp = true;
                    } else {
                        temp = false;
                        snipFirstClick = marker.getSnippet();
                    }
                }

                if (temp) {
                    if (marker.getSnippet() != null && "saigon-parking-parking-lot".equals(marker.getTag())) {
                        parkingLot = null;
                        try {
                            parkingLot = parkingLotServiceBlockingStub
                                    .getParkingLotById(Int64Value.newBuilder()
                                            .setValue(Long.parseLong(marker.getSnippet()))
                                            .build());
                            Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
                            intent.putExtra("parkingLot", (Serializable) parkingLot);
                            intent.putExtra("myLat", (Serializable) mLastKnownLocation.getLatitude());
                            intent.putExtra("myLong", (Serializable) mLastKnownLocation.getLongitude());

                            if (position3 != null) {
                                intent.putExtra("postion3lat", position3.latitude);
                                intent.putExtra("postion3long", position3.longitude);
                            }
                            startActivityWithLoading(intent);

                        } catch (StatusRuntimeException exception) {
                            saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
                        } catch (Exception e) {
                            Toast.makeText(MapActivity.this, "co loi load thong tin" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("loicuatoi", e.getCause() + "" + Long.parseLong(marker.getSnippet()));
                        }
                    }
                } else {
                    LatLng latLngOfPlace = marker.getPosition();
                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            latLngOfPlace, 18);
                    mMap.animateCamera(location, 500, null);
                }
//                Log.d("TTT",marker.getSnippet());
//                if(marker.getSnippet()!=null)
//                {
//                    final Results results = myPlaces.getResults()[Integer.parseInt(marker.getSnippet())];
//
//                    Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
//                    intent.putExtra("result", results);
//                    intent.putExtra("latt",Double.parseDouble(results.getGeometry().getLocation().getLat()));
//                    intent.putExtra("latt",Double.parseDouble(results.getGeometry().getLocation().getLng()));
//                    startActivity(intent);
//                }


                return true;
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {


            @Override
            public void onCameraChange(CameraPosition pos) {
                if (flatfunction == 0) {
                    return;
                }
                if (pos.zoom != currentZoom) {
                    currentZoom = pos.zoom;

                    LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
                    LatLng topLeft = curScreen.northeast;
                    LatLng rightBottom = curScreen.southwest;

                    float currentZoom = mMap.getCameraPosition().zoom;
//                    Toast.makeText(MapActivity.this, "You Zoom that: " +topLeft + " " + rightBottom, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, "You Zoom that: " + currentZoom, Toast.LENGTH_SHORT).show();
                    Log.d("XemZoom", "" + currentZoom);
                }
            }
        });
    }

    //handle loading screen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
        if (requestCode == RECOGNIZER_REQ_CODE) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                materialSearchBar.setText(result.get(0));
                materialSearchBar.enableSearch();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {

                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                longitude = mLastKnownLocation.getLongitude();
                                latitude = mLastKnownLocation.getLatitude();

                            } else {
                                LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(MapActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder builder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        builder.append("location=" + latitude + "," + longitude);
        builder.append("&radius=" + 10000);
        builder.append("&type=" + placeType);
        builder.append("&sensor=true");
        builder.append("&key=" + getResources().getString(R.string.google_maps_api));
        Log.d("APIURL", builder.toString());
        return builder.toString();
    }

    private void nearbyPlaces(String placeType) {
        mMap.clear();

        String url = getUrl(latitude, longitude, placeType);
        mGoogleApiService.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                        myPlaces = response.body();
                        if (response.isSuccessful()) {
                            for (int i = 0; i < response.body().getResults().length; i++) {


                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placeName = googlePlace.getName();
                                String vicinity = googlePlace.getVicinity();
                                LatLng latLng = new LatLng(lat, lng);
                                markerOptions.position(latLng);
                                markerOptions.title(placeName);

                                if (placeType.equals("hospital")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
                                } else if (placeType.equals("gas_station")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gas_pump));
                                } else if (placeType.equals("restaurant")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant));
                                } else {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
                                markerOptions.snippet(String.valueOf(i));


                                // add marker to map


                                marker = mMap.addMarker(markerOptions);

                                // move camera

                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });
    }


    @Override
    public void onTouchDown(MotionEvent event) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    @Override
    public void onTouchUp(MotionEvent event) {
        if (flatfunction != 0) {
            countDownTimer = new CountDownTimer(1000, 500) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    searchAnimationonMove.playAnimation();

                    if (flatfunction == 0) {
                        return;
                    }
                    LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
                    LatLng northEast = curScreen.northeast;
                    LatLng southWest = curScreen.southwest;
                    LatLng center = curScreen.getCenter();

                    if (northEast.equals(preNorthEast) && southWest.equals(preSouthWest)) {
                        return;
                    }

                    preNorthEast = northEast;
                    preSouthWest = southWest;

                    try {
                        if (modeParkingLot) {
                            Set<ParkingLotResult> differentSet = parkingLotServiceBlockingStub
                                    .getTopParkingLotInRegionOrderByDistanceWithoutName(ScanningByRadiusRequest.newBuilder()
                                            .setLatitude(center.latitude)
                                            .setLongitude(center.longitude)
                                            .setRadiusToScan(zoomRadiusMap.get((int) Math.floor(currentZoom)))
                                            .setNResult(zoomNResultMap.get((int) Math.floor(currentZoom)))
                                            .build())
                                    .getParkingLotResultList()
                                    .stream()
                                    .filter(parkingLot -> !parkingLotResultSet.contains(parkingLot))
                                    .collect(Collectors.toSet());

                            setAllMarkerParkingLot(differentSet);
                            parkingLotResultSet.addAll(differentSet);
                        }
                    } catch (StatusRuntimeException exception) {
                        saigonParkingExceptionHandler.handleCommunicationException(exception, MapActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(MapActivity.this, "ERROR scroll screen" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            countDownTimer.start();
        } else {

        }


//        Toast.makeText(MapActivity.this, "You Up that: " + northEast + " " + southWest, Toast.LENGTH_SHORT).show();
//        Log.d("vitridozoom", northEast + " , " + southWest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuoption, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_logout:
                saigonParkingDatabase.emptyTable();
                ((SaigonParkingApplication) getApplicationContext()).closeSocketConnection();
                Intent intent = new Intent(MapActivity.this, PermissionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_account:
                intent = new Intent(MapActivity.this, ProfileActivity.class);

                Pair<View, String> p1 = Pair.create((ImageView) profileLottie, "profileLottie");
                Pair<View, String> p2 = Pair.create((ImageView) saigonParking, "saigonParking");
                Pair<View, String> p3 = Pair.create((LinearLayout) lnHeader, "lnHeader");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, p3);
                startActivityWithLoadingAndOption(intent, options);
                break;
            case R.id.nav_history:
                Intent intenthistory = new Intent(MapActivity.this, HistoryActivity.class);
                startActivityWithLoading(intenthistory);
                break;
            case R.id.nav_setting:
                break;
        }
        return true;
    }
}