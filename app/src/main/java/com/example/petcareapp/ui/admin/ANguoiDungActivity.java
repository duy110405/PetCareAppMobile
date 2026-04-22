package com.example.petcareapp.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.User;
import com.example.petcareapp.utils.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ANguoiDungActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private TextView tvUserCount;
    private EditText edtSearch;

    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();

    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qly_user);

        rvUsers = findViewById(R.id.rvUsers);
        tvUserCount = findViewById(R.id.tvUserCount);
        edtSearch = findViewById(R.id.edtSearch);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuAdmin.setup(this, bottomNav);

        adapter = new UserAdapter(this, filteredList);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // SEARCH REALTIME
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    userList.clear();
                    filteredList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            userList.add(user);
                        }
                    }

                    filteredList.addAll(userList);

                    adapter.notifyDataSetChanged();
                    tvUserCount.setText(filteredList.size() + " người dùng");
                });
    }

    private void filterUsers(String keyword) {
        filteredList.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lower = keyword.toLowerCase().trim();

            for (User user : userList) {

                String name = user.getUsername() == null ? "" : user.getUsername().toLowerCase();
                String email = user.getEmail() == null ? "" : user.getEmail().toLowerCase();
                String phone = user.getPhone() == null ? "" : user.getPhone();

                if (name.contains(lower)
                        || email.contains(lower)
                        || phone.contains(lower)) {

                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();
        tvUserCount.setText(filteredList.size() + " người dùng");
    }
}
