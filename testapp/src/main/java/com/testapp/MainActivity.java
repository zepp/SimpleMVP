package com.testapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Consumer;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpActivity;

public class MainActivity extends MvpActivity<MvpPresenter<MainState>, MainState> {
    private InputMethodManager imm;
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
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        clearAll = findViewById(R.id.clear_all);
        viewPager.addOnPageChangeListener(new OnPageSelected(i -> {
            if (i == 1) {
                imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
            }
        }));
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

    private class OnPageSelected implements ViewPager.OnPageChangeListener {
        private final Consumer<Integer> consumer;

        public OnPageSelected(Consumer<Integer> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            consumer.accept(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }
}
