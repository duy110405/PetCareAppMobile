package com.example.petcareapp.ui.user;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.petcareapp.R;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UDaoChoiActivity extends AppCompatActivity {

    private TextView tvTime, tvDistance;
    private MaterialButton btnStartTracking, btnReset;
    private MaterialAutoCompleteTextView spinnerPet;

    // OSMDroid Map & Location
    private MapView mapView;
    private Polyline pathPolyline;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;

    // Tracking Data
    private boolean isTracking = false;
    private double totalDistance = 0.0; // Đơn vị: KM

    // Timer
    private int seconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // Firebase
    private FirebaseFirestore db;
    private String userId;
    private List<String> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cấu hình OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.user_dao_choi);

        // Ánh xạ View
        tvTime = findViewById(R.id.tvTime);
        tvDistance = findViewById(R.id.tvDistance);
        btnStartTracking = findViewById(R.id.btnStartTracking);
        btnReset = findViewById(R.id.btnReset);
        spinnerPet = findViewById(R.id.spinnerPet);
        mapView = findViewById(R.id.mapView);
        ImageView btnBack = findViewById(R.id.btnBack);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        MenuUser.setup(this, bottomNav);
        btnBack.setOnClickListener(v -> finish());

        // Setup Firebase  và Lấy danh sách Pet
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        loadPetsIntoSpinner();

        // Setup Bản đồ OSMDroid
        setupMap();

        //  Setup Cảm biến vị trí GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationTracking();
        setupTimer();

        // Xin quyền GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            zoomToCurrentLocation();
        }

        // Bắt sự kiện Nút Bấm
        btnStartTracking.setOnClickListener(v -> {
            if (!isTracking) startTracking();
            else stopTracking();
        });

        btnReset.setOnClickListener(v -> resetTracking());
    }

    // CÁC HÀM XỬ LÝ BẢN ĐỒ VÀ VỊ TRÍ
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint vietNamPoint = new GeoPoint(21.0285, 105.8542);
        mapView.getController().setZoom(20.0);
        mapView.getController().setCenter(vietNamPoint); // Ép bản đồ về VN ngay từ đầu

        // Setup nét vẽ đường đi (Màu xanh lam, nét to)
        pathPolyline = new Polyline();
        pathPolyline.getOutlinePaint().setColor(Color.parseColor("#2E64FE"));
        pathPolyline.getOutlinePaint().setStrokeWidth(15f);
        mapView.getOverlays().add(pathPolyline);
    }

    private void zoomToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mapView.getController().setCenter(startPoint);

                    // Thêm điểm đánh dấu vị trí hiện tại của mình
                    MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(mapView);
                    myLocationOverlay.enableMyLocation();
                    mapView.getOverlays().add(myLocationOverlay);
                }
            });
        }
    }

    private void setupLocationTracking() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    GeoPoint currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    // Thêm tọa độ vào nét vẽ bản đồ
                    pathPolyline.addPoint(currentPoint);
                    mapView.invalidate(); // Cập nhật lại bản đồ

                    // Di chuyển khung hình theo bước chân
                    mapView.getController().animateTo(currentPoint);

                    // Tính khoảng cách
                    if (lastLocation != null) {
                        float distanceInMeters = lastLocation.distanceTo(location);
                        totalDistance += (distanceInMeters / 1000.0); // Đổi ra KM
                        tvDistance.setText(String.format(Locale.getDefault(), "%.2f KM", totalDistance));
                    }
                    lastLocation = location;
                }
            }
        };
    }
    // BẮT ĐẦU / DỪNG / RESET
    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cần cấp quyền vị trí!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerPet.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thú cưng trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        isTracking = true;
        btnStartTracking.setText("Kết thúc dạo chơi");
        btnStartTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
        btnStartTracking.setIconResource(R.drawable.ic_close);

        // Cấu hình cập nhật vị trí mỗi 3 giây
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        timerHandler.post(timerRunnable);
    }

    private void stopTracking() {
        isTracking = false;
        btnStartTracking.setText("Bắt đầu theo dõi");
        btnStartTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#66BB6A")));
        btnStartTracking.setIconResource(R.drawable.ic_play);

        fusedLocationClient.removeLocationUpdates(locationCallback);
        timerHandler.removeCallbacks(timerRunnable);

        Toast.makeText(this, "Đã kết thúc! Quãng đường: " + String.format(Locale.getDefault(), "%.2f KM", totalDistance), Toast.LENGTH_LONG).show();
    }

    private void resetTracking() {
        if (isTracking) stopTracking();

        seconds = 0;
        totalDistance = 0.0;
        lastLocation = null;

        tvTime.setText("00:00");
        tvDistance.setText("0.00 KM");

        pathPolyline.getPoints().clear(); // Xóa đường đã vẽ
        mapView.invalidate();
    }

    // TIMER VÀ LOAD DỮ LIỆU
    private void setupTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int min = seconds / 60;
                int sec = seconds % 60;
                tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void loadPetsIntoSpinner() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("pets").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    petList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        petList.add(doc.getString("name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, petList);
                    spinnerPet.setAdapter(adapter);
                });
    }

    // Các hàm quan trọng để Bản đồ OSM không bị lỗi bộ nhớ
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}