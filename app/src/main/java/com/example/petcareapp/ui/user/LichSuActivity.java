package com.example.petcareapp.ui.user;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichSu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
public class LichSuActivity extends AppCompatActivity {
    EditText edtNgayTimKiem;
    Button btnTim;
    RecyclerView rcvLichSu;
    LichSuAdapter adapter;
    ArrayList<LichSu> danhSachLichSu;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    private DocumentSnapshot banGhiCuoiCung;
    private boolean dangTaiDuLieu= false;
    private boolean daHetDuLieu= false;
    private String ngayDangTimKiem= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);

        edtNgayTimKiem = findViewById(R.id.edtNgayTimKiem);
        btnTim = findViewById(R.id.btnTim);
        rcvLichSu= findViewById(R.id.rcvLichSu);
        db= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        danhSachLichSu= new ArrayList<>();
        adapter= new LichSuAdapter(danhSachLichSu);
        rcvLichSu.setLayoutManager(new LinearLayoutManager(this));
        rcvLichSu.setAdapter(adapter);
        loadDuLieuBanDau("");

        btnTim.setOnClickListener(view->{String ngayNhap= edtNgayTimKiem.getText().toString().trim();loadDuLieuBanDau(ngayNhap);
        });
        rcvLichSu.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx >0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager!=null) {
                        int viTriCuoiCungHienThi =layoutManager.findLastVisibleItemPosition();
                        int tongSoItem=layoutManager.getItemCount();
                        if (viTriCuoiCungHienThi==tongSoItem-1&&!dangTaiDuLieu && !daHetDuLieu) {
                            taiThemDuLieu(ngayDangTimKiem);
                        }
                    }
                }
            }
        });

    }
    private void loadDuLieuBanDau(String chuoiNgay) {
        if (mAuth.getCurrentUser() == null) return;

        danhSachLichSu.clear();
        adapter.notifyDataSetChanged();
        banGhiCuoiCung= null;
        daHetDuLieu=false;
        dangTaiDuLieu =true;
        ngayDangTimKiem= chuoiNgay;

        Query query= taoTruyVan(chuoiNgay);
        if (query==null) {
            dangTaiDuLieu = false;
            return;}

        query.limit(3).get().addOnSuccessListener(ketQua -> {
            for (QueryDocumentSnapshot doc:ketQua) {
                danhSachLichSu.add(doc.toObject(LichSu.class));
            }
            adapter.notifyDataSetChanged();
            if (ketQua.size()>0) banGhiCuoiCung=ketQua.getDocuments().get(ketQua.size() - 1);
            if (ketQua.size()<3) daHetDuLieu = true;
            dangTaiDuLieu = false;
            if(danhSachLichSu.isEmpty() && !chuoiNgay.isEmpty()){
                Toast.makeText(this, "không có dữ liệu trong ngày này", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> dangTaiDuLieu = false);
    }

    private void taiThemDuLieu(String chuoiNgay) {
        if (mAuth.getCurrentUser() == null || banGhiCuoiCung == null) return;

        dangTaiDuLieu = true;
        Query query = taoTruyVan(chuoiNgay);
        if (query == null) return;

        query.startAfter(banGhiCuoiCung)
                .limit(4)
                .get()
                .addOnSuccessListener(ketQua -> {
                    for (QueryDocumentSnapshot doc : ketQua) {
                        danhSachLichSu.add(doc.toObject(LichSu.class));
                    }
                    adapter.notifyDataSetChanged();

                    if (ketQua.size() > 0) {
                        banGhiCuoiCung = ketQua.getDocuments().get(ketQua.size() - 1);
                    }

                    if (ketQua.size() < 3) {
                        daHetDuLieu = true;
                        Toast.makeText(this, "dã hiển thị toàn bộ lịch sử", Toast.LENGTH_SHORT).show();
                    }
                    dangTaiDuLieu = false;
                }).addOnFailureListener(e -> dangTaiDuLieu = false);
    }
    private Query taoTruyVan(String chuoiNgay) {
        String uid = mAuth.getCurrentUser().getUid();
        Query query = db.collection("LichSuSuDung").whereEqualTo("maUser", uid);

        if (chuoiNgay.isEmpty()) {

            return query.orderBy("thoiGian", Query.Direction.DESCENDING);
        } else {
            try {
                SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy");
                Date date=sdf.parse(chuoiNgay);
                long batDauNgay=date.getTime();
                long ketThucNgay=batDauNgay+(24*60*60*1000)-1;

                return query.whereGreaterThanOrEqualTo("thoiGian", batDauNgay)
                        .whereLessThanOrEqualTo("thoiGian",ketThucNgay).orderBy("thoiGian",Query.Direction.DESCENDING);
            } catch (Exception e) {
                Toast.makeText(this,"nhập sai định dạng ngày", Toast.LENGTH_SHORT).show();
                dangTaiDuLieu = false;
                return null;
            }
        }
    }
}