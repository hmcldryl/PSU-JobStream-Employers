package com.jobstream.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.jobstream.employer.system.adapters.MainViewPagerAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    NavigationView navigationView;
    LinearLayout btnAnnouncements,
            btnJobPostings,
            btnSettings,
            btnAbout,
            btnSignOut;

    DrawerLayout drawerLayout;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    CircleImageView drawerPhoto;
    TextView drawerName,
            drawerEmail;

    MainViewPagerAdapter mainViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnAnnouncements = findViewById(R.id.btnAnnouncements);
        btnJobPostings = findViewById(R.id.btnJobPostings);
        btnSettings = findViewById(R.id.btnSettings);
        btnAbout = findViewById(R.id.btnAbout);
        btnSignOut = findViewById(R.id.btnSignOut);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        updateApp();

        mainViewPagerAdapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(mainViewPagerAdapter);

        drawerPhoto = findViewById(R.id.drawerPhoto);
        drawerName = findViewById(R.id.drawerName);
        drawerEmail = findViewById(R.id.drawerEmail);

        btnAnnouncements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AnnouncementActivity.class));
            }
        });

        btnJobPostings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, JobPostingActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    toolbar.setTitle(getString(R.string.toolbar_title_text_dashboard));
                    toolbar.setSubtitle(getString(R.string.toolbar_subtitle_text_home));
                } else if (position == 1) {
                    toolbar.setTitle(getString(R.string.toolbar_title_text_search));
                    toolbar.setSubtitle(getString(R.string.toolbar_subtitle_text_search));
                } else if (position == 2) {
                    toolbar.setTitle(getString(R.string.toolbar_title_text_messages));
                    toolbar.setSubtitle(getString(R.string.toolbar_subtitle_text_messages));
                } else if (position == 3) {
                    toolbar.setTitle(getString(R.string.toolbar_title_text_profile));
                    toolbar.setSubtitle(getString(R.string.toolbar_subtitle_text_profile));
                }
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_feed, null));
                        break;
                    case 1:
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_search, null));
                        break;
                    case 2:
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_message, null));
                        break;
                    case 3:
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_profile_employer, null));
                        break;
                }
            }
        }).attach();
    }

    private void updateApp() {
        AppUpdater appUpdater = new AppUpdater(this)
                .setTitleOnUpdateAvailable("Update Available")
                .setContentOnUpdateAvailable("Download the latest version of PSU JobStream for Employers.")
                .setContentOnUpdateNotAvailable("No Updates Available")
                .setTitleOnUpdateNotAvailable("You have the latest version of PSU JobStream for Employers.")
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("hmcldryl", "PSU-JobStream-Employers");
        appUpdater.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    db.collection("employers")
                            .document(auth.getCurrentUser().getUid())
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (value != null) {
                                        if (value.exists()) {
                                            if (value.getString("photoUrl") != null) {
                                                Glide.with(MainActivity.this)
                                                        .load(value.getString("photoUrl"))
                                                        .into(drawerPhoto);
                                            }
                                            drawerName.setText(value.getString("businessName"));
                                            drawerEmail.setText(value.getString("businessEmail"));
                                        }
                                    }
                                }
                            });
                } else {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        });
    }
}