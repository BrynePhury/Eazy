package com.prox.limeapp;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.prox.limeapp.Fragments.ContactsFragment;
import com.prox.limeapp.Fragments.NewsFeedFragment;
import com.prox.limeapp.Fragments.NotificationsFragment;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.Services.MyJobService;
import com.prox.limeapp.Utility.NotificationHelperClass;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    TextView toolbar_title;
    ImageView search;
    Toolbar toolbar;
    FrameLayout frameLayout;
    FloatingActionButton fab;
    public static BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    ImageView nav_profile_img;
    TextView nav_name;

    LinearLayout schoolLayout;
    LinearLayout classLayout;
    LinearLayout myKidsLayout;
    LinearLayout classesLayout;
    LinearLayout positionsLayout;
    LinearLayout approvalsLayout;


    NewsFeedFragment newsFeedFragment;
    ContactsFragment contactsFragment;
    NotificationsFragment notificationsFragment;

    boolean isNote;

    public static final int JOB_ID = 930;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        isNote = getIntent().getBooleanExtra("isIntent", false);

        toolbar_title = findViewById(R.id.toolbar_title);
        search = findViewById(R.id.search2);
        toolbar = findViewById(R.id.toolbar);
        frameLayout = findViewById(R.id.frameLayout);
        fab = findViewById(R.id.fab);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        nav_profile_img = findViewById(R.id.nav_profile_img);
        nav_name = findViewById(R.id.nav_user_profile_name);
        schoolLayout = findViewById(R.id.schoolLayout);
        classLayout = findViewById(R.id.classLayout);
        myKidsLayout = findViewById(R.id.myKidsLayout);
        approvalsLayout = findViewById(R.id.approvalsLayout);
        positionsLayout = findViewById(R.id.positionsLayout);
        classesLayout = findViewById(R.id.classesLayout);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        showUserInfo();

        bottomNavigationView.setItemBackgroundResource(R.color.white);

        newsFeedFragment = new NewsFeedFragment();
        contactsFragment = new ContactsFragment();
        notificationsFragment = new NotificationsFragment();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_home:
                        setFragment(newsFeedFragment);
                        break;
                    case R.id.bottom_nav_contacts:
                        setFragment(contactsFragment);
                        break;
                    case R.id.bottom_nav_notifications:
                        setFragment(notificationsFragment);
                        Prevalent.NOTIFICATION_COUNT = 0;
                        break;
                    case R.id.bottom_nav_profile:
                        startActivity(new Intent(HomeActivity.this, ProfileActivity.class)
                                .putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        break;
                }

                return true;
            }
        });


        scheduleJob();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            }
        });

    }

    private void showUserInfo() {
        if (Prevalent.CURRENT_USER != null){
            if (Prevalent.CURRENT_USER.getName() != null) {

                nav_name.setText(Prevalent.CURRENT_USER.getName());
            }
        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(nav_profile_img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(nav_profile_img);
                    }
                });
        if (Prevalent.CURRENT_USER.getRole() != null) {
            if (Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
                fab.setVisibility(View.GONE);
                schoolLayout.setVisibility(View.GONE);
                classLayout.setVisibility(View.GONE);
                classesLayout.setVisibility(View.GONE);
                approvalsLayout.setVisibility(View.GONE);
                positionsLayout.setVisibility(View.GONE);

            } else if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                schoolLayout.setVisibility(View.GONE);
                myKidsLayout.setVisibility(View.GONE);
                classesLayout.setVisibility(View.GONE);
                approvalsLayout.setVisibility(View.GONE);
                positionsLayout.setVisibility(View.GONE);

            } else if (Prevalent.CURRENT_USER.getRole().equals("Admin")) {
                myKidsLayout.setVisibility(View.GONE);
                classLayout.setVisibility(View.GONE);

            }
        } else {
            fab.setVisibility(View.GONE);
            schoolLayout.setVisibility(View.GONE);
            classLayout.setVisibility(View.GONE);
            classesLayout.setVisibility(View.GONE);
            approvalsLayout.setVisibility(View.GONE);
            positionsLayout.setVisibility(View.GONE);

        }
    }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isNote) {
            setFragment(notificationsFragment);
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_notifications);
        } else {
            setFragment(newsFeedFragment);
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);

        }
        DatabaseReference notificationRef =
                FirebaseDatabase.getInstance().getReference().child("Notifications");

        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                    assert notificationModel != null;
                    if (notificationModel.getNotificationTo() != null) {
                        if (notificationModel.getNotificationTo().equals(Prevalent.CURRENT_USER.getUid())
                                && !notificationModel.isSeen()) {
                            Prevalent.NOTIFICATION_COUNT = Prevalent.NOTIFICATION_COUNT + 1;
                        }
                    }

                }
                if (Prevalent.NOTIFICATION_COUNT > 0) {
                    bottomNavigationView.getOrCreateBadge(R.id.bottom_nav_notifications);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);

    }

    public void clickHome(View view) {
        closeDrawer(drawerLayout);
    }

    public void clickMyKids(View view) {
        startActivity(new Intent(HomeActivity.this, MyKidsActivity.class));
        closeDrawer(drawerLayout);

    }

    public void clickSchool(View view) {
        startActivity(new Intent(HomeActivity.this, SchoolActivity.class));
        closeDrawer(drawerLayout);
    }

    public void clickMyClass(View view) {
        startActivity(new Intent(HomeActivity.this, MyClassActivity.class));
        closeDrawer(drawerLayout);

    }

    public void clickClasses(View view) {
        startActivity(new Intent(HomeActivity.this, ClassesActivity.class));
        closeDrawer(drawerLayout);

    }

    public void clickApprovals(View view) {
        startActivity(new Intent(HomeActivity.this, ApprovalsActivity.class));
        closeDrawer(drawerLayout);

    }

    public void clickPositions(View view) {
        startActivity(new Intent(HomeActivity.this, PositionsActivity.class));
        closeDrawer(drawerLayout);

    }

    public void clickSignOut(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private static void closeDrawer(DrawerLayout drawerLayout) {

        //Close drawer layout
        //Check if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {

        }
    }

    private void scheduleJob() {
        ComponentName componentName = new ComponentName(this, MyJobService.class);

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(15 * 1000)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
    }
}