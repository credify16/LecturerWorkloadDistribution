package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Admin;
import com.example.credify.data.repository.AdminRepository;
import java.util.List;

public class AdminViewModel extends ViewModel {
    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<AdminState> adminState = new MutableLiveData<>(new AdminState.Idle());

    public LiveData<AdminState> getAdminState() {
        return adminState;
    }

    public void fetchAdmins() {
        adminState.setValue(new AdminState.Loading());
        repository.getAdmins().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                adminState.postValue(new AdminState.Empty());
            } else {
                adminState.postValue(new AdminState.Success(list));
            }
        }).exceptionally(ex -> {
            adminState.postValue(new AdminState.Error(ex.getMessage()));
            return null;
        });
    }

    public void addAdmin(String id, String name, String password, String email) {
        adminState.setValue(new AdminState.Loading());
        repository.addAdmin(id, name, password, email).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                adminState.postValue(new AdminState.ActionSuccess("Admin added successfully"));
            } else {
                adminState.postValue(new AdminState.Error(result));
            }
        });
    }

    public void updateAdmin(String id, String name, String email, String password) {
        adminState.setValue(new AdminState.Loading());
        repository.updateAdmin(id, name, email, password).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                adminState.postValue(new AdminState.ActionSuccess("Admin updated successfully"));
            } else {
                adminState.postValue(new AdminState.Error(result));
            }
        });
    }

    public void deleteAdmin(String id) {
        adminState.setValue(new AdminState.Loading());
        repository.deleteAdmin(id).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                adminState.postValue(new AdminState.ActionSuccess("Admin deleted successfully"));
            } else {
                adminState.postValue(new AdminState.Error(result));
            }
        });
    }
}
