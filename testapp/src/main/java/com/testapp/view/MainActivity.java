package com.testapp.view;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Consumer;
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
    private final static int FRAGMENT_TIMER = 0;
    private final static int FRAGMENT_MAIN = 1;
    private final static int FRAGMENT_EVENTS = 2;
    private InputMethodManager imm;
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
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenterImpl.class, MainState.class);
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
            if (i != FRAGMENT_MAIN) {
                imm.hideSoftInputFromWindow(viewPager.getApplicationWindowToken(), 0);
            }
        }));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        search = (SearchView) menu.findItem(R.id.main_search).getActionView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onStateChanged(MainState state) {
        search.setVisibility(state.currentPage == FRAGMENT_EVENTS ? View.VISIBLE : View.GONE);
        if (state.currentPage == FRAGMENT_EVENTS) {
            clearAll.show();
        } else {
            clearAll.hide();
        }
    }

    @Override
    public void onFirstStateChange(MainState state) {
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
                return MainFragment.newInstance(getPresenter().getId());
            } else if (i == FRAGMENT_TIMER) {
                return TimerFragment.newInstance(getPresenter().getId());
            } else {
                return EventsFragment.newInstance(getPresenter().getId());
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

    private static class OnPageSelected implements ViewPager.OnPageChangeListener {
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
