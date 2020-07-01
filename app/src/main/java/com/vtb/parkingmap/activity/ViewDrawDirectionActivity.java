package com.vtb.parkingmap.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bht.parkingmap.api.proto.parkinglot.ParkingLot;
import com.bht.parkingmap.api.proto.parkinglot.ParkingLotResult;
import com.bht.parkingmap.api.proto.parkinglot.ScanningByRadiusRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.ClassService.ClassService;
import com.vtb.parkingmap.Common;
import com.vtb.parkingmap.ParkingListAdapter;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingFragmentActivity;
import com.vtb.parkingmap.directionhelpers.FetchURL;
import com.vtb.parkingmap.directionhelpers.TaskLoadedCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.ghyeok.stickyswitch.widget.StickySwitch;


public class ViewDrawDirectionActivity extends BaseSaigonParkingFragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap mMap;
    private View mapView;
    private MarkerOptions place1, place2, place3;
    private boolean firstRender = true;
    Button getDirection;
    private Polyline currentPolyline;
    private Polyline currentPolyline2;
    private ArrayList<LatLng> points; //added
    Polyline line; //added
    private int flag_reached = 1; // Flag da den
    private int flag_Find_Destination = 0; // Flag on button draw diem thu 3
    private int flag_distance500 = 1; // Flag duoi 500m diem thu 3
    private Broadcast broadcast;

    double mylatfromplacedetail;
    double mylongfromplacedetail;
    double placedetaillat;
    double placedetaillong;

    double myposition3lat;
    double myposition3long;

    int type;
    long idplacedetail;

    List<ParkingLotResult> recommendedParkingLotResultList;
    ParkingListAdapter adapter;

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            double tmp = 1234;
            double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(place2.getPosition().latitude, place2.getPosition().longitude));
//            Toast.makeText(ViewDrawDirection.this, "" + distance, Toast.LENGTH_SHORT).show();
            place1 = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("My Location");
            if (distance < 500 && flag_distance500 == 1) {
                Toast.makeText(ViewDrawDirectionActivity.this, "Đã đến nơi", Toast.LENGTH_SHORT).show();
                if (myposition3lat != tmp) {
                    double distancewalk = SphericalUtil.computeDistanceBetween(new LatLng(myposition3lat, myposition3long), new LatLng(place2.getPosition().latitude, place2.getPosition().longitude));
                    if (distancewalk < 500) {
                        new FetchURL(ViewDrawDirectionActivity.this).execute(getUrl(place1.getPosition(), place3.getPosition(), "walking"), "walking");
                    } else {
                        flag_distance500 = 0;
                    }
                }
                flag_reached = 0;
            }
            if (flag_distance500 == 1 && flag_reached == 0 && myposition3lat != tmp) {
                new FetchURL(ViewDrawDirectionActivity.this).execute(getUrl(place1.getPosition(), place3.getPosition(), "walking"), "walking");
            }
            if (flag_Find_Destination == 0 && flag_reached == 1) {
                mLastLocation.set(location);
                new FetchURL(ViewDrawDirectionActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
            } else if (flag_Find_Destination == 1 && flag_distance500 == 0) {
                new FetchURL(ViewDrawDirectionActivity.this).execute(getUrl(place1.getPosition(), place3.getPosition(), "walking"), "walking");
            }
            if (flag_reached == 0 && flag_Find_Destination == 0 && myposition3lat != tmp) {
                currentPolyline.remove();
            }


        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        points = new ArrayList<>();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


        mylatfromplacedetail = (double) intent.getSerializableExtra("mylatfromplacedetail");
        mylongfromplacedetail = (double) intent.getSerializableExtra("mylongfromplacedetail");
        placedetaillat = (double) intent.getSerializableExtra("placedetaillat");
        placedetaillong = (double) intent.getSerializableExtra("placedetaillong");
        type = (int) intent.getSerializableExtra("placedetailtype");
        idplacedetail = (long) intent.getSerializableExtra("idplacedetail");


        myposition3lat = intent.getDoubleExtra("position3lat", 1234);
        myposition3long = intent.getDoubleExtra("position3long", 1234);

        broadcast = new Broadcast();
        IntentFilter filter = new IntentFilter("dialogFlag.Broadcast");
        registerReceiver(broadcast, filter);
        //Start service
        Intent myIntent = new Intent(ViewDrawDirectionActivity.this, ClassService.class);
        myIntent.putExtra("idplacedetail", (Serializable) idplacedetail);
        startService(myIntent);


        setContentView(R.layout.activity_view_draw_direction);
        getDirection = findViewById(R.id.btnGetDirection);
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ViewDrawDirectionActivity.this, "Test Service", Toast.LENGTH_SHORT).show();
            }
        });

        place1 = new MarkerOptions().position(new LatLng(mylatfromplacedetail, mylongfromplacedetail)).title("My Location");
        switch (type) {
            case 2:
                place2 = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.makerstreet))
                        .position(new LatLng(placedetaillat, placedetaillong)).title("Parking Lot ");

                break;
            case 1:
                place2 = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.makerprivate))
                        .position(new LatLng(placedetaillat, placedetaillong)).title("Parking Lot ");

                break;
            case 0:
                place2 = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.makerbuilding))
                        .position(new LatLng(placedetaillat, placedetaillong)).title("Parking Lot ");

                break;
            default:
                break;
        }

        double tmp = 1234;

        // Draw destination switch
        StickySwitch stickySwitchDestination = (StickySwitch) findViewById(R.id.draw_destination_onoff);
        stickySwitchDestination.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NonNull StickySwitch.Direction direction, @NonNull String text) {
                if (myposition3lat != tmp) {
                    if (stickySwitchDestination.getDirection() == StickySwitch.Direction.RIGHT && flag_reached == 1) {
                        Toast.makeText(ViewDrawDirectionActivity.this, "Please reach Parking Plot!", Toast.LENGTH_SHORT).show();
                        stickySwitchDestination.setDirection(StickySwitch.Direction.LEFT);
                    } else if (stickySwitchDestination.getDirection() == StickySwitch.Direction.RIGHT && flag_reached == 0) {
                        new FetchURL(ViewDrawDirectionActivity.this).execute(getUrl(place2.getPosition(), place3.getPosition(), "walking"), "walking");
                        flag_Find_Destination = 1;
                    } else if (stickySwitchDestination.getDirection() == StickySwitch.Direction.LEFT && flag_reached == 0) {
                        stickySwitchDestination.setDirection(StickySwitch.Direction.RIGHT);
                    }
                } else {
                    if (stickySwitchDestination.getDirection() == StickySwitch.Direction.RIGHT) {
                        Toast.makeText(ViewDrawDirectionActivity.this, "Please open option Find Destination!", Toast.LENGTH_SHORT).show();
                        stickySwitchDestination.setDirection(StickySwitch.Direction.LEFT);
                    }
                }
            }
        });

        if (myposition3lat != tmp) {
            place3 = new MarkerOptions().position(new LatLng(myposition3lat, myposition3long)).title("Destination 3 ");
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        //Switch Find destination
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 80, 180);


        mMap.addMarker(place1).showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place1.getPosition().latitude, place1.getPosition().longitude), 14));
        mMap.addMarker(place2).showInfoWindow();
        double tmp = 1234;
        if (myposition3lat != tmp) {
            mMap.addMarker(place3).showInfoWindow();
        }


    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    private String getUrl2(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url2 = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url2;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
//        currentPolyline2 = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // T2
        outState.putDouble("oldplace2lat", place2.getPosition().latitude);
        outState.putDouble("oldplace2long", place2.getPosition().longitude);

        if (place3 != null) {
            outState.putDouble("oldplace3lat", place3.getPosition().latitude);
            outState.putDouble("oldplace3long", place3.getPosition().longitude);
        } else {
            outState.putDouble("oldplace3lat", 1234);
            outState.putDouble("oldplace3long", 1234);
        }


        outState.putInt("flag_reached", flag_reached);
        outState.putInt("flag_Find_Destination", flag_Find_Destination);
        outState.putInt("flag_distance500", flag_distance500);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Toast.makeText(ViewDrawDirectionActivity.this, "Running old Stage", Toast.LENGTH_SHORT).show();
        placedetaillat = savedInstanceState.getDouble("oldplace2lat");
        placedetaillong = savedInstanceState.getDouble("oldplace2long");

        double tmppostion3lat = savedInstanceState.getDouble("oldplace3lat");
        double tmppostion3long = savedInstanceState.getDouble("oldplace3long");
        if (tmppostion3lat != 1234 && tmppostion3long != 1234) {
            myposition3lat = tmppostion3lat;
            myposition3long = tmppostion3long;
        }

        flag_reached = savedInstanceState.getInt("flag_reached");
        flag_Find_Destination = savedInstanceState.getInt("flag_Find_Destination");
        flag_distance500 = savedInstanceState.getInt("flag_distance500");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent myIntent = new Intent(ViewDrawDirectionActivity.this, ClassService.class);
        unregisterReceiver(broadcast);
        stopService(myIntent);
    }

    public class Broadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Broadcast.class.getSimpleName(), "Đã nhận được");
            Log.d("CCheck", placedetaillat + " " + placedetaillong);

            recommendedParkingLotResultList = new LinkedList<>();

            Objects.requireNonNull(recommendedParkingLotResultList)
                    .addAll(Common.parkingLotServiceBlockingStub
                            .getTopParkingLotInRegionOrderByDistanceWithName(ScanningByRadiusRequest.newBuilder()
                                    .setLatitude(placedetaillat)
                                    .setLongitude(placedetaillong)
                                    .setRadiusToScan(3)
                                    .setNResult(10)
                                    .build())
                            .getParkingLotResultList());


            List<ParkingLotResult> a = new ArrayList<>(recommendedParkingLotResultList);
            for (int i = 0; i < a.size(); i++) {
                Double distance = SphericalUtil.computeDistanceBetween(
                        new LatLng(placedetaillat, placedetaillong),
                        new LatLng(a.get(i).getLatitude(), a.get(i).getLongitude()));

                ParkingLotResult parkingLotResult = a.get(i).toBuilder()
                        .setDistance(distance / 1000)
                        .build();
                a.set(i, parkingLotResult);
            }

            adapter = new ParkingListAdapter(ViewDrawDirectionActivity.this, R.layout.adapter_view_layout, sortParkingLotResultList(a));

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ViewDrawDirectionActivity.this);
            builderSingle.setIcon(R.drawable.ic_draglocation_on);
            builderSingle.setTitle("Bãi xe hết chỗ");

            builderSingle.setNegativeButton("Về màn hình chính", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(ViewDrawDirectionActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            builderSingle.setCancelable(false);
            builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ParkingLot parkingLot;
                    ParkingLotResult selectedFromList = (ParkingLotResult) adapter.getItem(which);
                    parkingLot = Common.parkingLotServiceBlockingStub
                            .getParkingLotById(Int64Value.newBuilder()
                                    .setValue(selectedFromList.getId())
                                    .build());
                    Intent intent = new Intent(ViewDrawDirectionActivity.this, PlaceDetailsActivity.class);
                    Intent intent_placedetail = new Intent();
                    intent_placedetail.setAction("parkinglot_broadcast");
                    intent_placedetail.putExtra("parkinglot_broadcast", (Serializable) parkingLot);
                    intent_placedetail.putExtra("myLat_broadcast", (Serializable) mylatfromplacedetail);
                    intent_placedetail.putExtra("myLong_broadcast", (Serializable) mylongfromplacedetail);
                    if (place3 != null) {
                        intent_placedetail.putExtra("postion3lat_broadcast", place3.getPosition().latitude);
                        intent_placedetail.putExtra("postion3long_broadcast", place3.getPosition().longitude);
                    }
                    sendBroadcast(intent_placedetail);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }
            });
            builderSingle.show();

//            AlertDialog.Builder builder = new AlertDialog.Builder(ViewDrawDirection.this);
//            builder.setCancelable(false);
//            builder.setTitle("THÔNG BÁO !")
//                    .setMessage("BÃI XE ĐƯỢC CHỌN ĐÃ HẾT CHỖ")
//                    .setAdapter(1, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    })
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//
//                        }
//            });
//
//
//            builder.show();
        }
    }

    private List<ParkingLotResult> sortParkingLotResultList(List<ParkingLotResult> parkingLotResultList) {
        return parkingLotResultList.stream()
                .sorted(Comparator.comparing(ParkingLotResult::getDistance))
                .collect(Collectors.toList());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent myIntent = new Intent(ViewDrawDirectionActivity.this, ClassService.class);
        myIntent.putExtra("idplacedetail", (Serializable) idplacedetail);
        startService(myIntent);
    }
}
