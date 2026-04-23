package com.example.petcareapp.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Model đại diện cho một lịch hẹn trong hệ thống PetCare.
 *
 * <p>
 * Class này được sử dụng để:
 * <ul>
 *     <li>Lưu trữ thông tin lịch hẹn trên Firebase</li>
 *     <li>Hiển thị dữ liệu ở màn hình khách hàng và admin</li>
 *     <li>Phục vụ quy trình duyệt / từ chối lịch hẹn</li>
 * </ul>
 *
 * <p>
 * Bao gồm:
 * <ul>
 *     <li>Thông tin định danh (id, userId, petId, chiNhanhId)</li>
 *     <li>Thông tin cuộc hẹn (thời gian, dịch vụ, tổng tiền)</li>
 *     <li>Thông tin hiển thị bổ sung (tên thú cưng, chủ nuôi, chi nhánh)</li>
 *     <li>Trạng thái xử lý lịch hẹn</li>
 * </ul>
 */
public class LichHen {

    // =========================
    // Thông tin định danh
    // =========================

    /** ID duy nhất của lịch hẹn */
    private String id;

    /** ID người dùng đặt lịch */
    private String userId;

    /** ID thú cưng được đặt lịch */
    private String petId;

    /** ID chi nhánh thực hiện dịch vụ */
    private String chiNhanhId;

    // =========================
    // Thông tin lịch hẹn
    // =========================

    /** Thời gian khách hàng đặt hẹn */
    private Timestamp thoiGianHen;

    /** Danh sách dịch vụ khách hàng chọn */
    private List<DichVu> danhSachDichVu;

    /** Tổng chi phí của lịch hẹn */
    private int tongTien;

    /** Ghi chú bổ sung từ khách hàng */
    private String ghiChu;

    /** Lý do từ chối (chỉ dùng khi admin từ chối lịch hẹn) */
    private String lyDoTuChoi;

    /** Trạng thái lịch hẹn: Chờ duyệt / Đã duyệt / Từ chối / Hoàn thành */
    private String trangThai;

    // =========================
    // Thông tin hiển thị UI
    // =========================

    /** Tên thú cưng để hiển thị nhanh trên giao diện */
    private String tenThuCung;

    /** Tên chi nhánh hiển thị trên màn hình */
    private String tenChiNhanh;

    /** Tên chủ thú cưng */
    private String tenChuThuCung;

    /** Số điện thoại liên hệ của chủ thú cưng */
    private String soDienThoai;

    /**
     * Constructor rỗng bắt buộc cho Firebase Firestore.
     */
    public LichHen() {
    }

    /**
     * Constructor đầy đủ dùng khi khởi tạo object thủ công.
     *
     * @param id ID lịch hẹn
     * @param userId ID người dùng
     * @param petId ID thú cưng
     * @param chiNhanhId ID chi nhánh
     * @param thoiGianHen thời gian hẹn
     * @param danhSachDichVu danh sách dịch vụ
     * @param tongTien tổng tiền
     * @param ghiChu ghi chú từ khách hàng
     * @param lyDoTuChoi lý do từ chối
     * @param trangThai trạng thái lịch hẹn
     * @param tenThuCung tên thú cưng
     * @param tenChiNhanh tên chi nhánh
     */
    public LichHen(
            String id,
            String userId,
            String petId,
            String chiNhanhId,
            Timestamp thoiGianHen,
            List<DichVu> danhSachDichVu,
            int tongTien,
            String ghiChu,
            String lyDoTuChoi,
            String trangThai,
            String tenThuCung,
            String tenChiNhanh
    ) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.chiNhanhId = chiNhanhId;
        this.thoiGianHen = thoiGianHen;
        this.danhSachDichVu = danhSachDichVu;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
        this.lyDoTuChoi = lyDoTuChoi;
        this.trangThai = trangThai;
        this.tenThuCung = tenThuCung;
        this.tenChiNhanh = tenChiNhanh;
    }

    // =========================
    // Getter / Setter
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getChiNhanhId() {
        return chiNhanhId;
    }

    public void setChiNhanhId(String chiNhanhId) {
        this.chiNhanhId = chiNhanhId;
    }

    public Timestamp getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(Timestamp thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public List<DichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public void setDanhSachDichVu(List<DichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu;
    }

    public int getTongTien() {
        return tongTien;
    }

    public void setTongTien(int tongTien) {
        this.tongTien = tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTenChuThuCung() {
        return tenChuThuCung;
    }

    public void setTenChuThuCung(String tenChuThuCung) {
        this.tenChuThuCung = tenChuThuCung;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
}