package com.example.petcareapp.ui.user.TrangChu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.user.LichHen.AddLichHenActivity;
import com.example.petcareapp.ui.user.Pet.AddPetActivity;
import com.example.petcareapp.ui.user.Pet.PetAdapter;
import com.example.petcareapp.ui.user.Pet.PetDetailActivity;
import com.example.petcareapp.ui.user.Pet.PetViewModel;
//import com.example.petcareapp.utils.MenuUser;
import com.example.petcareapp.utils.LightSensorHelper;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UTrangChuActivity extends AppCompatActivity {

    private RecyclerView rvPets;
    private PetAdapter adapter;
    private PetViewModel viewModel;
    private TextView tvPetCount, tvUserName;
    private MaterialButton btnAddPet;
    private MaterialCardView cardDatLichKham, cardDaoChoi;
    private LightSensorHelper lightSensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_trang_chu);


        // 1. Ánh xạ các View từ Layout
        rvPets = findViewById(R.id.rvPets);
        tvPetCount = findViewById(R.id.tvPetCount);
        tvUserName = findViewById(R.id.tvUserName);
        btnAddPet = findViewById(R.id.btnAddPet);
        cardDatLichKham = findViewById(R.id.cardDatLichKham); // ID đã thêm ở XML
        cardDaoChoi = findViewById(R.id.cardDaoChoi);       // ID đã thêm ở XML
        lightSensorHelper = new LightSensorHelper(this);
        // Cấu hình Bottom Navigation thông qua lớp Utility MenuUser
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        MenuUser.setup(this, bottomNav);
        // 2. Thiết lập RecyclerView hiển thị danh sách thú cưng
        adapter = new PetAdapter();
        rvPets.setLayoutManager(new LinearLayoutManager(this));
        rvPets.setAdapter(adapter);

        // 3. Hiển thị tên người dùng hiện tại từ Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            tvUserName.setText(displayName != null && !displayName.isEmpty() ? displayName : user.getEmail());
        }

        // 4. Khởi tạo và kết nối ViewModel
        viewModel = new ViewModelProvider(this).get(PetViewModel.class);
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.loadPets(userId);
        }

        // Lắng nghe dữ liệu Pet thay đổi để cập nhật giao diện
        viewModel.getPets().observe(this, pets -> {
            if (pets != null) {
                adapter.setData(pets);
                tvPetCount.setText(String.valueOf(pets.size()));
            }
        });

        // 5. Xử lý các sự kiện nhấn (Click Events)

        // Mở màn hình Thêm thú cưng
        btnAddPet.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPetActivity.class));
        });

        // Nhấn vào một thú cưng trong danh sách để xem chi tiết
        adapter.setOnItemClickListener(pet -> {
            Intent intent = new Intent(UTrangChuActivity.this, PetDetailActivity.class);
            intent.putExtra("petId", pet.getId());
            startActivity(intent);
        });

        // Nhấn thẻ "Đặt lịch khám" -> Chuyển sang màn hình Lịch hẹn
        if (cardDatLichKham != null) {
            cardDatLichKham.setOnClickListener(v -> {
                startActivity(new Intent(UTrangChuActivity.this, AddLichHenActivity.class));
            });
        }

        // Nhấn thẻ "Theo dõi dạo chơi" -> Chuyển sang màn hình Dạo chơi
        if (cardDaoChoi != null) {
            cardDaoChoi.setOnClickListener(v -> {
                //startActivity(new Intent(UTrangChuActivity.this, UDaoChoiActivity.class));
            });
        }


    }

    // Cập nhật lại danh sách khi quay lại từ các màn hình khác
    @Override
    protected void onResume() {
        super.onResume();
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.loadPets(userId);
        }
        if (lightSensorHelper != null) {
            lightSensorHelper.register();
        }

        checkThongBao();
    }
    private void checkThongBao() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) return;

        com.google.firebase.firestore.FirebaseFirestore
                .getInstance()
                .collection("ThongBao")
                .whereEqualTo("userId", userId)
                .whereEqualTo("daDoc", false)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) return;

                    StringBuilder message = new StringBuilder();

                    for (var doc : snapshot.getDocuments()) {
                        String noiDung = doc.getString("noiDung");

                        if (noiDung != null) {
                            message.append("• ")
                                    .append(noiDung)
                                    .append("\n\n");
                        }

                        doc.getReference().update("daDoc", true);
                    }

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Thông báo")
                            .setMessage(message.toString())
                            .setPositiveButton("OK", null)
                            .show();
                });
    }
    protected void onPause() {
        super.onPause();
        if (lightSensorHelper != null) {
            lightSensorHelper.unregister();
        }
    }
}