package com.example.petcareapp.ui.user.TimPhong;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.data.repository.UChiNhanhRepository;

import java.util.List;

public class UChiNhanhViewModel extends ViewModel {
    private UChiNhanhRepository repository;
    private MutableLiveData<List<ChiNhanh>> danhSachChiNhanh;

    public UChiNhanhViewModel() {
        repository = new UChiNhanhRepository();
    }
    public LiveData<List<ChiNhanh>> getDanhSachChiNhanh() {
        if(danhSachChiNhanh == null) {
            danhSachChiNhanh = new MutableLiveData<>();
            repository.layDanhSachChiNhanh(danhSachChiNhanh);
        }
        return danhSachChiNhanh;
    }

}
