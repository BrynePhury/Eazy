package com.prox.limeapp.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.prox.limeapp.R;

public class CreateSchoolDialog extends AppCompatDialogFragment {
    public TextInputEditText schoolNameInput;
    private Button saveButton;

    private CreateSchoolListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.create_school_dialog,null);

        builder.setView(view);
        schoolNameInput = view.findViewById(R.id.schoolName_input);

        saveButton = view.findViewById(R.id.save_school_btn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String className_str = schoolNameInput.getText().toString();

                listener.applyTexts(className_str);
            }
        });


        return builder.create();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (CreateSchoolListener) context;
        } catch (ClassCastException exception) {

            throw new ClassCastException(context.toString() +
                    " must implement NewBudgetDialogListener");
        }
    }

    public interface CreateSchoolListener {
        void applyTexts(String name);
    }
}
