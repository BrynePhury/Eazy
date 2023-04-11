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

public class AddPositionDialog extends AppCompatDialogFragment {
    public TextInputEditText positionName;
    private Button saveButton;

    private AddPositionDialogListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_position_dialog,null);

        builder.setView(view);
        positionName = view.findViewById(R.id.position_input);

        saveButton = view.findViewById(R.id.save_position_btn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String className_str = positionName.getText().toString();

                listener.applyTexts(className_str);
            }
        });


        return builder.create();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddPositionDialogListener) context;
        } catch (ClassCastException exception) {

            throw new ClassCastException(context.toString() +
                    " must implement AddPositionDialogListener");
        }
    }

    public interface AddPositionDialogListener {
        void applyTexts(String name);
    }
}
