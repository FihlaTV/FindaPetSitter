package org.finalappproject.findapetsitter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;

import org.finalappproject.findapetsitter.R;
import org.finalappproject.findapetsitter.adapters.SittersAdapter;
import org.finalappproject.findapetsitter.model.User;
import org.finalappproject.findapetsitter.util.recyclerview.DividerItemDecoration;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvailableSittersFragment extends UserListFragment {

    private static final String LOG_TAG = "AvailableSittersFrag";

    private LinearLayoutManager mAvailableSittersLinearlayout;
    private LinkedList<User> mAvailableSittersList;  //TODO: if we want to display search results based on distance, do we need linkedlist or arraylist is ok?
    private SittersAdapter mAvailableSittersAdapter;

    @BindView(R.id.rvAvailableSitters)
    RecyclerView mAvailableSittersRecyclerView;
    @BindView(R.id.swipeContainerAvailableSitters)
    SwipeRefreshLayout mAvailableSittersSwipeRefreshLayout;

    @Override
    void populateList() {
        User.queryPetSitters(new FindCallback<User>() {
            public void done(List<User> petSitters, ParseException e) {
                if (e == null) {
                    User curUser = (User) User.getCurrentUser();
                    for (User u : petSitters) {
                        if (u.getObjectId().equals(curUser.getObjectId())) {
                            continue;
                        }
                        mAvailableSittersList.add(u);
                    }
                    mAvailableSittersAdapter.notifyDataSetChanged();
                } else {
                    Log.e(LOG_TAG, "Failed to fetch pet sitters", e);
                    Toast.makeText(getContext(), "Failed to fetch pet sitters", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);

        mAvailableSittersList = new LinkedList<>();
        mAvailableSittersAdapter = new SittersAdapter(getContext(), mAvailableSittersList);

        View availableSittersView = inflater.inflate(R.layout.fragment_available_sitters, parent, false);
        ButterKnife.bind(this, availableSittersView);

        mAvailableSittersRecyclerView.setAdapter(mAvailableSittersAdapter);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        mAvailableSittersRecyclerView.addItemDecoration(itemDecoration);

        mAvailableSittersLinearlayout = new LinearLayoutManager(getActivity());
        mAvailableSittersLinearlayout.setOrientation(LinearLayoutManager.VERTICAL);
        mAvailableSittersLinearlayout.scrollToPosition(0);
        mAvailableSittersRecyclerView.setLayoutManager(mAvailableSittersLinearlayout);

        mAvailableSittersSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAvailableSittersList.clear();
                mAvailableSittersAdapter.notifyDataSetChanged();
                populateList();
                mAvailableSittersSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mAvailableSittersList.clear();
        return availableSittersView;
    }
}