package com.example.petcareapp.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.data.repository.AChiNhanhRepository;

import java.util.List;

public class AChiNhanhViewModel extends ViewModel {
    private AChiNhanhRepository repository;
    private MutableLiveData<String> trangThaiThemChiNhanh;
    private MutableLiveData<List<ChiNhanh>> danhSachChiNhanh;

    public AChiNhanhViewModel() {
        repository = new AChiNhanhRepository();
        trangThaiThemChiNhanh = new MutableLiveData<>();
    }

    public LiveData<String> getTrangThaiThemChiNhanh() {
        return trangThaiThemChiNhanh;
    }
    // Hàm nhận dữ liệu từ màn hình và gói lại
    public void taoMoiChiNhanh(String ten, String diaChi, String sdt, String gio, double viDo, double kinhDo) {
        ChiNhanh chiNhanhMoi = new ChiNhanh(ten, diaChi, sdt, gio, viDo, kinhDo);
        repository.themChiNhanhLenFirebase(chiNhanhMoi, trangThaiThemChiNhanh);
    }
    public LiveData<List<ChiNhanh>> getDanhSachChiNhanh() {
        if(danhSachChiNhanh == null) {
            danhSachChiNhanh = new MutableLiveData<>();
            repository.layDanhSachChiNhanh(danhSachChiNhanh);
        }
        return danhSachChiNhanh;
    }
    public void capNhatChiNhanh(ChiNhanh cn) {
        repository.suaChiNhanhTrenFirebase(cn, trangThaiThemChiNhanh); // Dùng chung biến trạng thái cho tiện
    }

    public void xoaChiNhanh(String id) {
        repository.xoaChiNhanhTrenFirebase(id, trangThaiThemChiNhanh);
    }
}