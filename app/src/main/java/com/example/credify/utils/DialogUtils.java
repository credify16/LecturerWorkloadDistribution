package com.example.credify.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.example.credify.databinding.DialogConfirmDeleteBinding;
import com.example.credify.databinding.DialogSuccessBinding;

public class DialogUtils {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public static void showSuccessDialog(Activity activity, String title, String message, Runnable onOk) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogSuccessBinding binding = DialogSuccessBinding.inflate(LayoutInflater.from(activity));
        dialog.setContentView(binding.getRoot());
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        binding.tvSuccessTitle.setText(title);
        binding.tvSuccessMessage.setText(message);
        binding.btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (onOk != null) onOk.run();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void showDeleteConfirmationDialog(Activity activity, String message, OnConfirmListener listener) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogConfirmDeleteBinding binding = DialogConfirmDeleteBinding.inflate(LayoutInflater.from(activity));
        dialog.setContentView(binding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (message != null) {
            binding.tvDeleteMessage.setText(message);
        }
        
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        binding.btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        dialog.show();
    }
}
