package com.testapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpActivity;

public class MainActivity extends MvpActivity<MvpPresenter<MainState>, MainState> {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton clearAll;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_main;
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenter.class, MainState.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        clearAll = findViewById(R.id.clear_all);
    }

    @Override
    public void onStateChanged(MainState state) {
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        clearAll.setOnClickListener(getMvpListener());
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return MainFragment.newInstance(getPresenter().getId());
            } else {
                return EventsFragment.newInstance(getPresenter().getId());
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "main";
            } else {
                return "events";
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
