package com.example.petcareapp.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.repository.DichVuRepository;

import java.util.List;

public class ADichVuViewModel extends ViewModel {

    private final DichVuRepository repo = new DichVuRepository();
    private final MutableLiveData<String> thongBao = new MutableLiveData<>();

    public LiveData<List<DichVu>> getDanhSachDichVu() {
        return repo.getAllDichVu();
    }

    public LiveData<String> getThongBao() {
        return thongBao;
    }

    public void themDichVu(String ten, String moTa, double gia) {
        DichVu dv = new DichVu(null, ten, moTa, gia);
        repo.addDichVu(dv, new DichVuRepository.Callback() {
            @Override
            public void onSuccess() {
                thongBao.setValue("Thêm thành công");
            }

            @Override
            public void onFailure(String err) {
                thongBao.setValue("Lỗi: " + err);
            }
        });
    }

    public void capNhatDichVu(DichVu dv) {
        repo.updateDichVu(dv, new DichVuRepository.Callback() {
            @Override
            public void onSuccess() {
                thongBao.setValue("Cập nhật thành công");
            }

            @Override
            public void onFailure(String err) {
                thongBao.setValue("Lỗi: " + err);
            }
        });
    }

    public void xoaDichVu(String id) {
        repo.deleteDichVu(id, new DichVuRepository.Callback() {
            @Override
            public void onSuccess() {
                thongBao.setValue("Xóa thành công");
            }

            @Override
            public void onFailure(String err) {
                thongBao.setValue("Lỗi: " + err);
            }
        });
    }
}