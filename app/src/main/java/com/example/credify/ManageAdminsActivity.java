package com.example.credify;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.Admin;
import com.example.credify.databinding.ActivityManageAdminsBinding;
import com.example.credify.databinding.ItemAdminBinding;
import com.example.credify.viewmodel.AdminState;
import com.example.credify.viewmodel.AdminViewModel;

import java.util.ArrayList;
import java.util.List;

public class ManageAdminsActivity extends AppCompatActivity {

    private ActivityManageAdminsBinding binding;
    private AdminViewModel viewModel;

    private List<Admin> adminList = new ArrayList<>();
    private List<Admin> filteredList = new ArrayList<>();

    private AdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAdminsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this)
                .get(AdminViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupActions();
        observeViewModel();

        viewModel.fetchAdmins();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {

        adapter = new AdminAdapter(filteredList);

        binding.rvAdmins.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvAdmins.setAdapter(adapter);
    }

    private void setupSearch() {

        binding.etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filter(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );
    }

    private void setupActions() {

        binding.btnAddAdmin.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            AddAdminActivity.class
                    )
            );

        });
    }

    private void observeViewModel() {

        viewModel.getAdminState().observe(this, state -> {

            if (state instanceof AdminState.Loading) {

                binding.progressBar
                        .setVisibility(View.VISIBLE);

                binding.layoutEmpty
                        .setVisibility(View.GONE);

            } else if (state instanceof AdminState.Success) {

                binding.progressBar
                        .setVisibility(View.GONE);

                adminList.clear();

                adminList.addAll(
                        ((AdminState.Success) state)
                                .getAdmins()
                );

                filter(binding.etSearch
                        .getText()
                        .toString());

            } else if (state instanceof AdminState.Empty) {

                binding.progressBar
                        .setVisibility(View.GONE);

                adminList.clear();

                filter(binding.etSearch
                        .getText()
                        .toString());

            } else if (state instanceof AdminState.ActionSuccess) {

                binding.progressBar
                        .setVisibility(View.GONE);

                Toast.makeText(
                        this,
                        ((AdminState.ActionSuccess) state)
                                .getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                viewModel.fetchAdmins();

            } else if (state instanceof AdminState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        this,
                        ((AdminState.Error) state).getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void filter(String query) {

        filteredList.clear();

        if (query.isEmpty()) {

            filteredList.addAll(adminList);

        } else {

            String lowerCaseQuery =
                    query.toLowerCase();

            for (Admin admin : adminList) {

                if (admin.getAdminName()
                        .toLowerCase()
                        .contains(lowerCaseQuery)
                        ||
                        admin.getAdminID()
                                .toLowerCase()
                                .contains(lowerCaseQuery)
                ) {

                    filteredList.add(admin);
                }
            }
        }

        adapter.notifyDataSetChanged();

        checkEmptyState();
    }

    private void checkEmptyState() {

        if (filteredList.isEmpty()) {

            binding.layoutEmpty
                    .setVisibility(View.VISIBLE);

            binding.rvAdmins
                    .setVisibility(View.GONE);

        } else {

            binding.layoutEmpty
                    .setVisibility(View.GONE);

            binding.rvAdmins
                    .setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmation(
            String adminId,
            String adminName
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Admin")
                .setMessage(
                        "Are you sure you want to delete admin:\n\n"
                                + adminName
                                + " ?"
                )
                .setPositiveButton("Delete",
                        (dialog, which) -> {

                            viewModel.deleteAdmin(adminId);

                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.fetchAdmins();
    }

    // =========================
    // Adapter
    // =========================

    private class AdminAdapter
            extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

        private final List<Admin> items;

        AdminAdapter(List<Admin> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public AdminViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            ItemAdminBinding b =
                    ItemAdminBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    );

            return new AdminViewHolder(b);
        }

        @Override
        public void onBindViewHolder(
                @NonNull AdminViewHolder holder,
                int position
        ) {

            Admin admin = items.get(position);

            holder.binding.tvAdminName
                    .setText(admin.getAdminName());

            holder.binding.tvAdminId
                    .setText("ID: " + admin.getAdminID());

            holder.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(
                        ManageAdminsActivity.this,
                        EditAdminActivity.class
                );

                intent.putExtra(
                        "admin_id",
                        admin.getAdminID()
                );

                intent.putExtra(
                        "admin_name",
                        admin.getAdminName()
                );

                intent.putExtra(
                        "admin_email",
                        admin.getEmail()
                );

                startActivity(intent);
            });

            holder.binding.btnDelete.setOnClickListener(v -> {

                showDeleteConfirmation(
                        admin.getAdminID(),
                        admin.getAdminName()
                );

            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class AdminViewHolder
                extends RecyclerView.ViewHolder {

            ItemAdminBinding binding;

            AdminViewHolder(ItemAdminBinding b) {
                super(b.getRoot());
                this.binding = b;
            }
        }
    }
}