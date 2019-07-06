package autoservisi.Autoservisi.MakinaIme.fragments;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import autoservisi.Autoservisi.MakinaIme.R;

public class StatisticsFragment extends Fragment {

    public static final String TAG = "StatisticsFragment";

    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;

    public StatisticsFragment() {}


    class ViewPagerAdapter extends FragmentPagerAdapter{

        private final ArrayList<Fragment> fragmentList = new ArrayList<>();
        private final ArrayList<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.app_bar_layout);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.vp_statistics);
        tabLayout = new TabLayout(getContext());
        appBarLayout.addView(tabLayout);
        int color = ResourcesCompat.getColor(getResources(), R.color.colorTextIcons, null);
        tabLayout.setTabTextColors(color, color);
        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        Bundle bundle = getArguments();
        LineChartFragment fragment = new LineChartFragment();
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "Shpenzimet");
        FuelPriceFragment fuelPriceFragment = new FuelPriceFragment();
        fuelPriceFragment.setArguments(bundle);
        adapter.addFragment(fuelPriceFragment, "Cmimi");
        viewPager.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        appBarLayout.removeView(tabLayout);
    }
}
