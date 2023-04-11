package com.prox.limeapp.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.prox.limeapp.R;

public class AddClassDialog extends AppCompatDialogFragment {

    public TextInputEditText className;
    private Button saveButton;

    private AddClassDialogListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_class_dialog,null);

        builder.setView(view);
        className = view.findViewById(R.id.class_name_input);

        saveButton = view.findViewById(R.id.save_class_btn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String className_str = className.getText().toString();

                listener.applyTexts(className_str);
            }
        });


        return builder.create();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddClassDialogListener) context;
        } catch (ClassCastException exception) {

            throw new ClassCastException(context.toString() +
                    " must implement NewBudgetDialogListener");
        }
    }

    public interface AddClassDialogListener {
        void applyTexts(String name);
    }
}
