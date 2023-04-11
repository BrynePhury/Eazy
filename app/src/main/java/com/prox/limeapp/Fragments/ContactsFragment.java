package com.prox.limeapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.ContactsAdapter;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    Context context;
    TextView contacts_title;
    RecyclerView contacts_rcy;
    TextView default_text;

    ContactsAdapter contactsAdapter;

    List<User> contacts = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contacts_title = view.findViewById(R.id.contacts_title);
        contacts_rcy = view.findViewById(R.id.contacts_rcy);
        default_text = view.findViewById(R.id.default_text);

        contactsAdapter = new ContactsAdapter(contacts);

        contacts_rcy.setLayoutManager(new LinearLayoutManager(context));
        contacts_rcy.setAdapter(contactsAdapter);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getListData();
    }

    @Override
    public void onPause() {
        super.onPause();
        contacts.clear();
        contactsAdapter.notifyDataSetChanged();
    }

    private void getListData() {
        contacts.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users");

        reference.child("Guardians")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                User user = dataSnapshot.getValue(User.class);
                                assert user != null;
                                if (user.getSchoolId() != null && Prevalent.CURRENT_USER.getSchoolId()
                                        != null && user.getUid() != null) {
                                    if (!user.getUid().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (user.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                                            if (user.getRole() != null && Prevalent.CURRENT_USER.getRole() != null) {
                                                if (Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
                                                    if (!user.getRole().equals("Guardian") &&
                                                            user.getState().equals("Approved")) {
                                                        contacts_title.setVisibility(View.VISIBLE);
                                                        contacts_rcy.setVisibility(View.VISIBLE);
                                                        default_text.setVisibility(View.GONE);

                                                        contacts.add(user);
                                                        contactsAdapter.notifyDataSetChanged();
                                                    }
                                                } else if (Prevalent.CURRENT_USER.getRole().equals("Admin") ||
                                                        Prevalent.CURRENT_USER.getRole().equals("Teacher")) {

                                                    contacts_title.setVisibility(View.VISIBLE);
                                                    contacts_rcy.setVisibility(View.VISIBLE);
                                                    default_text.setVisibility(View.GONE);

                                                    contacts.add(user);
                                                    contactsAdapter.notifyDataSetChanged();
                                                } else {
                                                    if (!user.getRole().equals("Guardian")) {
                                                        contacts_title.setVisibility(View.VISIBLE);
                                                        contacts_rcy.setVisibility(View.VISIBLE);
                                                        default_text.setVisibility(View.GONE);

                                                        contacts.add(user);
                                                        contactsAdapter.notifyDataSetChanged();
                                                    } else {
                                                        if (user.getClassId() != null &&
                                                                user.getClassId().equals(Prevalent.CURRENT_USER.getClassId())) {
                                                            contacts_title.setVisibility(View.VISIBLE);
                                                            contacts_rcy.setVisibility(View.VISIBLE);
                                                            default_text.setVisibility(View.GONE);

                                                            contacts.add(user);
                                                            contactsAdapter.notifyDataSetChanged();

                                                        }
                                                    }
                                                }

                                            }
                                        } else {
                                            if (Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
                                                if (!user.getRole().equals("Guardian")) {
                                                    if (user.getSchoolId().equals(Prevalent.CURRENT_USER.getSecondSchoolId())
                                                            || user.getSchoolId().equals(Prevalent.CURRENT_USER.getThirdSchoolId())
                                                            || user.getSchoolId().equals(Prevalent.CURRENT_USER.getFourthSchoolId())
                                                            || user.getSchoolId().equals(Prevalent.CURRENT_USER.getFifthSchoolId())) {
                                                        contacts_title.setVisibility(View.VISIBLE);
                                                        contacts_rcy.setVisibility(View.VISIBLE);
                                                        default_text.setVisibility(View.GONE);

                                                        contacts.add(user);
                                                        contactsAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
