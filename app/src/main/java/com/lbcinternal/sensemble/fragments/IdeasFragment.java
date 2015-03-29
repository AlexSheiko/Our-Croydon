package com.lbcinternal.sensemble.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lbcinternal.sensemble.R;
import com.lbcinternal.sensemble.activities.DetailActivity;
import com.lbcinternal.sensemble.activities.MainActivity;
import com.lbcinternal.sensemble.adapters.IdeasAdapter;
import com.lbcinternal.sensemble.rest.ApiService;
import com.lbcinternal.sensemble.rest.RestClient;
import com.lbcinternal.sensemble.rest.model.IdeaEntry;
import com.lbcinternal.sensemble.views.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class IdeasFragment extends Fragment {

    private List<IdeaEntry> mEntries;

    public IdeasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getFragmentManager()));

        SlidingTabLayout tabBar = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        tabBar.setDistributeEvenly(true);
        tabBar.setViewPager(pager);
        tabBar.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tab_indicator);
            }
        });

        final ListView feedListView = (ListView) rootView.findViewById(R.id.list_view);

        ApiService service =
                new RestClient(getActivity()).getApiService();
        service.getIdeas(new ResponseCallback() {
            @Override public void success(Response response) {
                mEntries = new ArrayList<>();

                String sampleXml = null;
                try {
                    InputStream inputStream = response.getBody().in();

                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder in = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        in.append(line);
                    }
                    sampleXml = in.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray ideasArray = new JSONArray(sampleXml);
                    for (int i = 0; i < ideasArray.length(); i++) {
                        JSONObject entry = ideasArray.getJSONObject(i);

                        String title = entry.getString("Title");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date date = null;
                        try {
                            date = format.parse(entry.getString("DateCreated"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        mEntries.add(new IdeaEntry(title, date));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (getActivity() != null) {
                    feedListView.setAdapter(new IdeasAdapter(getActivity(),
                            mEntries));
                    feedListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override public void onItemClick(AdapterView<?> parent, View view,
                                                          int position, long id) {
                            Date creationDate = mEntries.get(position).getCreationDate();
                            DateFormat format = new SimpleDateFormat("F MMM");
                            String date = format.format(creationDate);
                            String title = mEntries.get(position).getTitle();

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            sp.edit()
                                    .putString("section", "ideas")
                                    .putString("title", title)
                                    .putString("date", date)
                                    .apply();

                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });

        return rootView;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity())
                .getSupportActionBar().setTitle("Ideas");
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private static final int NUM_ITEMS = 2;
        private String[] mTabTitles;

        public PagerAdapter(FragmentManager fm) {
            super(fm);

            mTabTitles = new String[]{
                    "Most recent".toUpperCase(),
                    "Highest rated".toUpperCase()
            };
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return new IdeasFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }
}
