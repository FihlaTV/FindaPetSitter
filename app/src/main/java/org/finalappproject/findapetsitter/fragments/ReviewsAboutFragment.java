package org.finalappproject.findapetsitter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import org.finalappproject.findapetsitter.adapters.ReviewsAboutAdapter;
import org.finalappproject.findapetsitter.model.Review;
import org.finalappproject.findapetsitter.model.User;
import org.finalappproject.findapetsitter.util.recyclerview.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static java.security.AccessController.getContext;

public class ReviewsAboutFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FindCallback<Review>{

    private static final String LOG_TAG = "ReviewsAboutFragment";

    private LinearLayoutManager mReviewsAboutLinearlayout;
    private ArrayList<Review> mReviewsAbout;

    private ReviewsAboutAdapter mReviewsAboutAdapter;
    User mUser;
    private Unbinder mUnbinder;

    @BindView(R.id.rvReviewsAbout)
    RecyclerView rvReviewsAbout;
    @BindView(R.id.swipeContainerReviewsAbout)
    SwipeRefreshLayout mReviewsAboutSwipeRefreshLayout;

    public static ReviewsAboutFragment newInstance() {
        ReviewsAboutFragment fragment = new ReviewsAboutFragment();
        // Add arguments
        Bundle savedInstanceState = new Bundle();
        //savedInstanceState.putBoolean(ARGUMENT_IS_SITTER_FLOW, isPetSitterFlow);
        //fragment.setArguments(savedInstanceState);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize member variables
        mReviewsAbout = new ArrayList<>();
        mReviewsAboutAdapter = new ReviewsAboutAdapter(getContext(), mReviewsAbout);
        mUser = (User) User.getCurrentUser();
        fetchReviews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_reviews_about, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        setupRecyclerView();
        setupSwipeRefresh();

        return view;
    }

    void setupRecyclerView() {
        rvReviewsAbout.setAdapter(mReviewsAboutAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvReviewsAbout.addItemDecoration(itemDecoration);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rvReviewsAbout.setLayoutManager(linearLayoutManager);
    }

    void setupSwipeRefresh() {
        mReviewsAboutSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onRefresh() {
        int reviewsCount = mReviewsAbout.size();
        mReviewsAbout.clear();
        mReviewsAboutAdapter.notifyItemRangeRemoved(0, reviewsCount);
        fetchReviews();
        mReviewsAboutSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchReviews() {
        Review.queryByReviewReceiver(mUser, this);
    }

    @Override
    public void done(List<Review> objects, ParseException e) {

        if (e == null) {
            for (Review review : objects) {
                try {
                    review.getReviewer().fetchIfNeeded(); // TODO: sure?
                } catch (ParseException ex) {
                    // This will occur when users associated to the request have been deleted
                    continue;
                }
                mReviewsAbout.add(review);
            }
            mReviewsAboutAdapter.notifyDataSetChanged();
        } else {
            Log.e(LOG_TAG, "Failed to fetch request", e);
            Toast.makeText(getContext(), "Failed to fetch requests", Toast.LENGTH_LONG).show();
        }
    }
}