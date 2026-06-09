package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Admin;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.repository.UserRepository;

public class LoginViewModel extends ViewModel {
    private final UserRepository repository = new UserRepository();
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>(new LoginState.Idle());

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        loginState.setValue(new LoginState.Loading());
        repository.login(email, password).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                detectRole();
            } else {
                loginState.postValue(new LoginState.Error(result));
            }
        });
    }

    public void checkSession() {
        if (repository.isUserLoggedIn()) {
            detectRole();
        }
    }

    private void detectRole() {
        loginState.postValue(new LoginState.Loading());
        repository.detectRole().thenAccept(role -> {
            if (role instanceof Admin) {
                loginState.postValue(new LoginState.SuccessAdmin());
            } else if (role instanceof Lecturer) {
                Lecturer lecturer = (Lecturer) role;
                String actualRole = lecturer.getLecturerRole();
                android.util.Log.d("LoginViewModel", "Role detection: Lecturer found. Role string: '" + actualRole + "'");
                
                boolean isCoordinator = false;
                if (actualRole != null) {
                    actualRole = actualRole.trim();
                    if (actualRole.equalsIgnoreCase("Course Coordinator") || 
                        actualRole.equalsIgnoreCase("Coordinator")) {
                        isCoordinator = true;
                    }
                }

                if (isCoordinator) {
                    android.util.Log.d("LoginViewModel", "Redirecting to Coordinator Dashboard");
                    loginState.postValue(new LoginState.SuccessCoordinator(lecturer));
                } else {
                    android.util.Log.d("LoginViewModel", "Redirecting to Lecturer Dashboard. Role was: " + actualRole);
                    loginState.postValue(new LoginState.SuccessLecturer(lecturer));
                }
            } else if (role == null) {
                android.util.Log.d("LoginViewModel", "Role detection: role is null");
                loginState.postValue(new LoginState.Error("Login successful, but no role found in database. Please contact admin."));
            } else {
                android.util.Log.d("LoginViewModel", "Role detection: unexpected role type: " + role.getClass().getName());
                loginState.postValue(new LoginState.Error("An unexpected error occurred during role detection."));
            }
        });
    }

    public void signUp(String email, String password) {
        loginState.setValue(new LoginState.Loading());
        repository.signUp(email, password).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                loginState.postValue(new LoginState.SignUpSuccess());
            } else {
                loginState.postValue(new LoginState.Error(result));
            }
        });
    }

    public void resetPassword(String email) {
        loginState.setValue(new LoginState.Loading());
        repository.resetPassword(email).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                loginState.postValue(new LoginState.OtpSent());
            } else {
                loginState.postValue(new LoginState.Error(result));
            }
        });
    }

    public void verifyOtpAndUpdatePassword(String email, String code, String newPassword) {
        if (newPassword.length() < 6) {
            loginState.setValue(new LoginState.Error("Password must be at least 6 characters."));
            return;
        }

        loginState.setValue(new LoginState.Loading());
        repository.verifyOtp(email, code).thenAccept(verifyResult -> {
            if ("SUCCESS".equals(verifyResult)) {
                repository.updatePassword(newPassword).thenAccept(updateResult -> {
                    if ("SUCCESS".equals(updateResult)) {
                        loginState.postValue(new LoginState.PasswordUpdated());
                    } else {
                        loginState.postValue(new LoginState.Error(updateResult));
                    }
                });
            } else {
                loginState.postValue(new LoginState.Error(verifyResult));
            }
        });
    }
}
