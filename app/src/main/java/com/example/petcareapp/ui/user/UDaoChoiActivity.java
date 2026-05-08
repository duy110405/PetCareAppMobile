package com.example.petcareapp.ui.user;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.KhoVoucher;
import com.example.petcareapp.data.model.Voucher;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
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

    // ===== Biến cho phần Voucher =====
    private TextView tvCurrentPoints;
    private RecyclerView rvVouchers;
    private int currentPoints = 0;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.user_dao_choi);

        // Ánh xạ View Tracking
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

        // Setup Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        loadPetsIntoSpinner();

        // ===== Ánh xạ , Cài đặt khu vực Voucher =====
        tvCurrentPoints = findViewById(R.id.tvCurrentPoints);
        rvVouchers = findViewById(R.id.rvVouchers);

        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        voucherAdapter = new VoucherAdapter();
        rvVouchers.setAdapter(voucherAdapter);

        loadUserPoints(); // Tải điểm lúc mới vào trang
        loadVouchers();   // Tải danh sách voucher
        // ===========================================

        // Setup Bản đồ OSMDroid
        setupMap();

        // Setup Cảm biến vị trí GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationTracking();
        setupTimer();

        // Xin quyền GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            zoomToCurrentLocation();
        }

        // Bắt sự kiện Nút Bấm Tracking
        btnStartTracking.setOnClickListener(v -> {
            if (!isTracking) startTracking();
            else stopTracking();
        });

        btnReset.setOnClickListener(v -> resetTracking());
    }

    // 1. CÁC HÀM XỬ LÝ VOUCHER & ĐIỂM
    private void loadUserPoints() {
        if (userId == null) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long diem = document.getLong("tongDiem");
                        currentPoints = diem != null ? diem.intValue() : 0;
                        if (tvCurrentPoints != null) {
                            tvCurrentPoints.setText(String.valueOf(currentPoints));
                        }
                    }
                });
    }

    private void loadVouchers() {
        db.collection("Voucher").get()
                .addOnSuccessListener(snapshot -> {
                    voucherList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Voucher v = doc.toObject(Voucher.class);
                        if (v != null) {
                            v.setIdVoucher(doc.getId());
                            voucherList.add(v);
                        }
                    }
                    voucherAdapter.notifyDataSetChanged();
                });
    }

    private void handleExchangeVoucher(Voucher voucher) {
        if (currentPoints < voucher.getDiemYeuCau()) {
            Toast.makeText(this, "Bạn không đủ điểm để đổi voucher này!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi")
                .setMessage("Bạn có chắc muốn dùng " + voucher.getDiemYeuCau() + " điểm để đổi voucher: " + voucher.getTenVoucher() + "?")
                .setPositiveButton("Đổi ngay", (dialog, which) -> executeExchangeTransaction(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeExchangeTransaction(Voucher voucher) {
        WriteBatch batch = db.batch();

        // 1. Trừ điểm của User
        batch.update(db.collection("users").document(userId),
                "tongDiem", FieldValue.increment(-voucher.getDiemYeuCau()));

        // 2. Thêm vào KhoVoucher
        String khoId = db.collection("KhoVoucher").document().getId();
        KhoVoucher khoVoucher = new KhoVoucher(khoId, voucher.getIdVoucher(), userId, "Chưa dùng");
        batch.set(db.collection("KhoVoucher").document(khoId), khoVoucher);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đổi Voucher thành công! Kiểm tra khi đặt lịch nhé.", Toast.LENGTH_LONG).show();
                    loadUserPoints(); // Tải lại điểm trên giao diện ngay lập tức
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi đổi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //  CÁC HÀM XỬ LÝ BẢN ĐỒ VÀ VỊ TRÍ
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        GeoPoint vietNamPoint = new GeoPoint(21.0285, 105.8542);
        mapView.getController().setZoom(20.0);
        mapView.getController().setCenter(vietNamPoint);

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

                    pathPolyline.addPoint(currentPoint);
                    mapView.invalidate();
                    mapView.getController().animateTo(currentPoint);

                    if (lastLocation != null) {
                        float distanceInMeters = lastLocation.distanceTo(location);
                        totalDistance += (distanceInMeters / 1000.0);
                        tvDistance.setText(String.format(Locale.getDefault(), "%.2f KM", totalDistance));
                    }
                    lastLocation = location;
                }
            }
        };
    }

    // TRACKING LOGIC (BẮT ĐẦU / DỪNG / RESET)
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

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        timerHandler.post(timerRunnable);
    }

    private int calculatePoints(double distance) {
        if (distance < 1.0) {
            return 0;
        } else if (distance <= 3.0) {
            return (int) (distance * 10);
        } else {
            return (int) (distance * 15);
        }
    }

    private void stopTracking() {
        isTracking = false;
        btnStartTracking.setText("Bắt đầu theo dõi");
        btnStartTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#66BB6A")));
        btnStartTracking.setIconResource(R.drawable.ic_play);

        fusedLocationClient.removeLocationUpdates(locationCallback);
        timerHandler.removeCallbacks(timerRunnable);

        int diemThuong = calculatePoints(totalDistance);

        if (diemThuong > 0 && userId != null) {
            db.collection("users").document(userId)
                    .update("tongDiem", FieldValue.increment(diemThuong))
                    .addOnSuccessListener(aVoid -> {
                        // Tính tổng điểm mới và cập nhật UI ngay lập tức
                        int newTotalPoints = currentPoints + diemThuong;
                        currentPoints = newTotalPoints;
                        if (tvCurrentPoints != null) {
                            tvCurrentPoints.setText(String.valueOf(currentPoints));
                        }

                        // Kiểm tra xem điểm mới này có đủ đổi BẤT KỲ voucher nào trong kho không
                        boolean canRedeemAnyVoucher = false;
                        for (Voucher v : voucherList) {
                            if (newTotalPoints >= v.getDiemYeuCau()) {
                                canRedeemAnyVoucher = true;
                                break; // Chỉ cần đủ điểm đổi 1 cái là dừng vòng lặp
                            }
                        }

                        // Hiển thị thông báo dựa trên việc có đủ điểm đổi quà hay không
                        if (canRedeemAnyVoucher) {
                            // Đủ điểm -> Hiển thị Popup chúc mừng hoành tráng
                            new AlertDialog.Builder(UDaoChoiActivity.this)
                                    .setTitle("🎉 Chúc mừng bạn!")
                                    .setMessage(String.format(Locale.getDefault(),
                                            "Bạn vừa đi được %.2f KM và nhận +%d điểm.\n\nĐặc biệt: Bạn đã đủ điểm để đổi Voucher rồi đấy! Hãy lướt xuống Cửa hàng để xem ngay nhé.",
                                            totalDistance, diemThuong))
                                    .setPositiveButton("Xem Cửa hàng", (dialog, which) -> {
                                        // Tự động cuộn màn hình xuống chỗ Cửa hàng Voucher
                                        rvVouchers.getParent().requestChildFocus(rvVouchers, rvVouchers);
                                    })
                                    .setNegativeButton("Đóng", null)
                                    .show();
                        } else {
                            // Chưa đủ điểm đổi quà -> Hiện Toast thông báo cộng điểm bình thường
                            String msg = String.format(Locale.getDefault(),
                                    "Hoàn thành: %.2f KM\nTuyệt vời! Bạn nhận được +%d điểm \uD83C\uDF89",
                                    totalDistance, diemThuong);
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể cập nhật điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            String msg = String.format(Locale.getDefault(),
                    "Đã kết thúc: %.2f KM\nHãy cố gắng đi bộ hơn 1 KM để nhận điểm nhé!",
                    totalDistance);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void resetTracking() {
        if (isTracking) stopTracking();

        seconds = 0;
        totalDistance = 0.0;
        lastLocation = null;

        tvTime.setText("00:00");
        tvDistance.setText("0.00 KM");

        pathPolyline.getPoints().clear();
        mapView.invalidate();
    }

    // TIMER VÀ LOAD DỮ LIỆU PET
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

    // Vòng đời Bản đồ OSM
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

    // INNER CLASS: ADAPTER CHO VOUCHER
    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Voucher v = voucherList.get(position);

            holder.tvTenVoucher.setText(v.getTenVoucher());
            holder.tvGiamGia.setText(String.format("Giảm: %,d đ", v.getSoTienGiam()));
            holder.tvDiemYeuCau.setText(v.getDiemYeuCau() + " Điểm");

            holder.btnDoiVoucher.setOnClickListener(view -> handleExchangeVoucher(v));
        }

        @Override
        public int getItemCount() {
            return voucherList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTenVoucher, tvGiamGia, tvDiemYeuCau;
            MaterialButton btnDoiVoucher;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTenVoucher = itemView.findViewById(R.id.tvTenVoucher);
                tvGiamGia = itemView.findViewById(R.id.tvGiamGia);
                tvDiemYeuCau = itemView.findViewById(R.id.tvDiemYeuCau);
                btnDoiVoucher = itemView.findViewById(R.id.btnDoiVoucher);
            }
        }
    }
}