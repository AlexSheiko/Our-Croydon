package com.lbcinternal.sensemble.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lbcinternal.sensemble.R;
import com.lbcinternal.sensemble.activities.DetailActivity;
import com.lbcinternal.sensemble.adapters.IdeasAdapter;
import com.lbcinternal.sensemble.rest.ApiService;
import com.lbcinternal.sensemble.rest.RestClient;
import com.lbcinternal.sensemble.rest.model.Idea;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class IdeasPageFragment extends Fragment {

    public IdeasPageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ideas_page, container, false);


        if (getArguments().getString("query") == null) {

            int order = getArguments().getInt("order");
            ApiService service = new RestClient()
                    .getApiService();
            service.getIdeas(order, getResponseCallback(rootView));

        } else {

            String query = getArguments().getString("query");
            ApiService service = new RestClient()
                    .getApiService();
            service.findIdeas(query, getResponseCallback(rootView));

        }

        return rootView;
    }

    private Callback<List<Idea>> getResponseCallback(final View rootView) {
        Callback<List<Idea>> callback = new Callback<List<Idea>>() {
            @Override public void success(final List<Idea> ideas, Response response) {
                if (getActivity() != null) {
                    ListView feedListView = (ListView) rootView.findViewById(
                            R.id.list_view);

                    feedListView.setAdapter(new IdeasAdapter(getActivity(),
                            ideas));
                    feedListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override public void onItemClick(AdapterView<?> parent, View view,
                                                          int position, long id) {
                            Date creationDate = ideas.get(position).getCreationDate();
                            DateFormat format = new SimpleDateFormat("F MMM");
                            String date = format.format(creationDate);
                            String title = ideas.get(position).getTitle();
                            String entryId = ideas.get(position).getId();

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            sp.edit()
                                    .putString("section", "ideas")
                                    .putString("ideaTitle", title)
                                    .putString("ideaId", entryId)
                                    .putString("ideaDate", date)
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
        };
        return callback;
    }
}
