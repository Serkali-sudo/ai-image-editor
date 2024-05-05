package com.serhat.aieditor;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.serhat.aieditor.viewpager.FragmentPagerItem;
import com.serhat.aieditor.viewpager.FragmentPagerItems;

import java.lang.ref.WeakReference;

public class MainViewPager2Adapter extends FragmentStateAdapter {

    private final FragmentPagerItems pages;

    private final SparseArrayCompat<WeakReference<Fragment>> holder;

    public MainViewPager2Adapter(FragmentActivity fm, FragmentPagerItems pages) {
        super(fm);
        this.pages = pages;
        this.holder = new SparseArrayCompat<>(pages.size());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return getPagerItem(position).instantiate(pages.getContext(), position);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    protected FragmentPagerItem getPagerItem(int position) {
        return pages.get(position);
    }

}
