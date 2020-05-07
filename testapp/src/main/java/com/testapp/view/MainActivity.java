/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */

package com.testapp.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpActivity;
import com.testapp.R;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainPresenterImpl;
import com.testapp.presenter.MainState;

public class MainActivity extends MvpActivity<MainPresenter, MainState> {
    public final static int FRAGMENT_TIMER = 0;
    public final static int FRAGMENT_MAIN = 1;
    public final static int FRAGMENT_EVENTS = 2;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton clearAll;
    private SearchView search;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_main;
    }

    @Override
    @NonNull
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenterImpl.class, MainState.class);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        search = (SearchView) menu.findItem(R.id.main_search).getActionView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onStateChanged(@NonNull MainState state) {
        search.setVisibility(state.currentPage == FRAGMENT_EVENTS ? View.VISIBLE : View.GONE);
        if (state.currentPage == FRAGMENT_EVENTS) {
            clearAll.show();
        } else {
            clearAll.hide();
        }
    }

    @Override
    public void onFirstStateChange(@NonNull MainState state) {
        super.onFirstStateChange(state);
        clearAll.setOnClickListener(getMvpListener());
        search.setOnQueryTextListener(newQueryTextListener(search));
        viewPager.addOnPageChangeListener(newOnPageChangeListener(viewPager));
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == FRAGMENT_MAIN) {
                return MainFragment.newInstance();
            } else if (i == FRAGMENT_TIMER) {
                return TimerFragment.newInstance();
            } else {
                return EventsFragment.newInstance();
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == FRAGMENT_MAIN) {
                return getString(R.string.fragment_main);
            } else if (position == FRAGMENT_TIMER) {
                return getString(R.string.fragment_timer);
            } else {
                return getString(R.string.fragment_events);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
