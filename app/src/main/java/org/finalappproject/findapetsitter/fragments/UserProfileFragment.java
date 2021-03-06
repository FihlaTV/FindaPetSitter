package org.finalappproject.findapetsitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import org.finalappproject.findapetsitter.R;
import org.finalappproject.findapetsitter.activities.UserProfileEditActivity;
import org.finalappproject.findapetsitter.adapters.PetsAdapter;
import org.finalappproject.findapetsitter.adapters.ReviewsAboutAdapter;
import org.finalappproject.findapetsitter.model.Address;
import org.finalappproject.findapetsitter.model.Pet;
import org.finalappproject.findapetsitter.model.Review;
import org.finalappproject.findapetsitter.model.User;
import org.finalappproject.findapetsitter.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.finalappproject.findapetsitter.model.User.queryUser;

/**
 * User Profile fragment, used to display the user profile of current user and pet sitters
 */
public class UserProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FindCallback<Review> {

    private static final String LOG_TAG = "UserProfileFragment";

    @BindView(R.id.ivUserProfileImage)
    ImageView ivUserProfileImage;

    @BindView(R.id.tvUserName)
    TextView etUserName;

    @BindView(R.id.tvUserNickname)
    TextView etUserNickname;

    @BindView(R.id.tvUserAddress)
    TextView tvUserAddress;

    @BindView(R.id.tvUserDescription)
    TextView etUserDescription;

    @BindView(R.id.rvPets)
    RecyclerView rvPets;

    @BindView(R.id.btWriteReview)
    Button btWriteReview;

    @BindView(R.id.ivDiagonalProfile)
    ImageView ivDiagonalProfile;

    String mUserObjectId;
    User mUser;
    List<Pet> mPets;
    PetsAdapter mPetsAdapter;

    private ArrayList<Review> mReviewsAbout;
    private ReviewsAboutAdapter mReviewsAboutAdapter;

    @BindView(R.id.rvReviewsAbout)
    RecyclerView rvReviewsAbout;
    @BindView(R.id.swipeContainerReviewsAbout)
    SwipeRefreshLayout mReviewsAboutSwipeRefreshLayout;


    /**
     * Required empty public constructor,
     * use newInstance() factory method instead
     */
    @Deprecated
    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method used to create SitterHomeFragment
     * with parameters, use this method instead of
     * new SitterHomeFragment()
     *
     * @return A new instance of fragment SitterHomeFragment.
     */
    public static UserProfileFragment newInstance(String userId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPets = new ArrayList<>();
        mPetsAdapter = new PetsAdapter(getContext(), mPets);

        mReviewsAbout = new ArrayList<>();
        mReviewsAboutAdapter = new ReviewsAboutAdapter(getContext(), mReviewsAbout);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.bind(this, view);
        setupPetsRecyclerView();
        setupReviewsRecyclerView();
        setupReviewsSwipeRefresh();

        mUserObjectId = getArguments().getString("user_id");

        if (!isCurrentUser()) {
            setupWriteReviewButton();
        }

        if (mUserObjectId != null && !mUserObjectId.isEmpty()) {
            queryUser(mUserObjectId, new GetCallback<User>() {
                @Override
                public void done(User user, ParseException e) {
                    if (e == null) {
                        mUser = user;
                        loadData();
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch user", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            mUser = (User) User.getCurrentUser();
            loadData();
            fetchReviews();
        }

        return view;
    }

    private boolean isCurrentUser() {
        // mUserObjectId will be null if the fragment hasn't received a user_id as parameter, we will use the current user then
        return (mUserObjectId == null);
    }


    @Override
    public void onRefresh() {
        fetchReviews();
    }

    private void fetchReviews() {
        Review.queryByReviewReceiver(mUser, this);
    }

    @Override
    public void done(List<Review> objects, ParseException e) {

        if (e == null) {
            int oldReviewsCount = mReviewsAbout.size();
            mReviewsAbout.clear();
            mReviewsAbout.addAll(objects);
            int newReviewsCount = mReviewsAbout.size();

            if (newReviewsCount == oldReviewsCount) {
                mReviewsAboutAdapter.notifyItemRangeChanged(0, newReviewsCount);
            } else if (newReviewsCount > oldReviewsCount) {
                mReviewsAboutAdapter.notifyItemRangeChanged(0, oldReviewsCount);
                mReviewsAboutAdapter.notifyItemRangeInserted(oldReviewsCount, (newReviewsCount - oldReviewsCount));
            } else {
                if (newReviewsCount != 0) {
                    mReviewsAboutAdapter.notifyItemRangeChanged(0, newReviewsCount);
                }
                mReviewsAboutAdapter.notifyItemRangeRemoved(newReviewsCount, (oldReviewsCount - newReviewsCount));
            }

            mReviewsAboutSwipeRefreshLayout.setRefreshing(false);
        } else {
            Log.e(LOG_TAG, "Failed to fetch request", e);
            Toast.makeText(getContext(), "Failed to fetch requests", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (isCurrentUser()) {
            inflater.inflate(R.menu.menu_profile, menu);
        } else {
            inflater.inflate(R.menu.menu_sitter, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.miSendRequest:
                onMenuSendRequestClick();
                return true;
            case R.id.miEdit:
                onMenuItemEditClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuItemEditClick() {
        Intent editProfileIntent = new Intent(getContext(), UserProfileEditActivity.class);
        startActivity(editProfileIntent);

        // TODO refresh user
    }

    private void onMenuSendRequestClick() {
        Bundle bundle = new Bundle();
        bundle.putString("sitter_id", mUser.getObjectId());

        RequestFragment requestFragmentDialog = new RequestFragment();
        requestFragmentDialog.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        requestFragmentDialog.show(fm, "request");
    }

    private void setupWriteReviewButton() {
        btWriteReview.setVisibility(View.VISIBLE);
        btWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReviewClick();
            }
        });
    }

    private void onReviewClick() {
        Bundle bundle = new Bundle();
        bundle.putString("sitter_id", mUser.getObjectId());

        WriteReviewFragment reviewFragmentDialog = new WriteReviewFragment();
        reviewFragmentDialog.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        reviewFragmentDialog.show(fm, "write_review");
    }

    private void setupPetsRecyclerView() {
        rvPets.setAdapter(mPetsAdapter);
        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(getContext());
        linerLayoutManager.setOrientation(LinearLayout.HORIZONTAL);
        rvPets.setLayoutManager(linerLayoutManager);
    }

    void setupReviewsRecyclerView() {
        rvReviewsAbout.setAdapter(mReviewsAboutAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rvReviewsAbout.setLayoutManager(linearLayoutManager);
    }

    void setupReviewsSwipeRefresh() {
        mReviewsAboutSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void loadData() {
        // TODO check, this seems to be a hack, because onResume is calling loadData
        if (mUser == null) {
            return;
        }
        fetchReviews();
        etUserName.setText(mUser.getFullName());
        etUserNickname.setText(mUser.getNickName());
        etUserDescription.setText(mUser.getDescription());
        mUser.getAddress().fetchIfNeededInBackground(new GetCallback<Address>() {
            @Override
            public void done(Address address, ParseException e) {
                if (e == null) {
                    tvUserAddress.setText(getString(R.string.lives_in_content, address.getCity(), address.getState()));
                }
            }
        });

        // Show user profile image
        ImageHelper.loadImage(getContext(), mUser.getProfileImage(), R.drawable.ic_person, ivUserProfileImage);
        Glide.with(getActivity()).load(R.drawable.cat_and_dog_profile_bg).into(ivDiagonalProfile);
        loadPetsData();

    }

    private void loadPetsData() {
        // Clear existent
        int petsCount = mPets.size();
        mPets.clear();
        mPetsAdapter.notifyItemRangeRemoved(0, petsCount);
        // Add pets
        List<Pet> userPets = mUser.getPets();
        if (userPets != null) {
            mPets.addAll(userPets);
            mPetsAdapter.notifyItemRangeInserted(0, userPets.size());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

}
