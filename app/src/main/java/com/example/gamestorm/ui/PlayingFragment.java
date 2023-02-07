package com.example.gamestorm.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.gamestorm.adapter.RecyclerData;
import com.example.gamestorm.adapter.RecyclerProfileViewAdapter;
import com.example.gamestorm.model.GameApiResponse;
import com.example.gamestorm.R;
import com.example.gamestorm.repository.GamesRepository;
import com.example.gamestorm.repository.IGamesRepository;
import com.example.gamestorm.databinding.FragmentPlayingBinding;
import com.example.gamestorm.util.ResponseCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlayingFragment extends Fragment {

    FragmentPlayingBinding binding;
    FirebaseAuth firebaseAuth;
    Button loginButton;
    ConstraintLayout function_not_available_layout;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    private RecyclerView recyclerView;
    private ArrayList<RecyclerData> recyclerDataArrayList;
    private ProgressBar progressBar;
    String loggedUserID;
    ArrayList<Integer> playingGames;
    FirebaseFirestore firebaseFirestore;
    ConstraintLayout playing_games_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=FragmentPlayingBinding.inflate(getLayoutInflater());
        firebaseAuth=FirebaseAuth.getInstance();
        function_not_available_layout=requireView().findViewById(R.id.function_not_available_layout);
        loginButton=requireView().findViewById(R.id.loginButton);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("1091326442567-dbkvi0h9877eego2ou819bepnb05h65g.apps.googleusercontent.com").requestEmail().build();
        gsc = GoogleSignIn.getClient(requireContext(),gso);
        firebaseFirestore=FirebaseFirestore.getInstance();
        playing_games_layout = requireView().findViewById(R.id.playing_games_layout);

        if(!isLogged()){
            function_not_available_layout.setVisibility(View.VISIBLE);
            playing_games_layout.setVisibility(View.GONE);
        }else{
            function_not_available_layout.setVisibility(View.GONE);
            playing_games_layout.setVisibility(View.VISIBLE);
        }

        loginButton.setOnClickListener(view1 -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_loginActivity);
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if(isNetworkAvailable(requireContext())) {
            if (isLogged()) {
                viewGames();
            }
        }else{
            Snackbar.make(requireView().findViewById(R.id.Coordinatorlyt), R.string.no_connection_message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void viewGames() {
        DocumentReference docRef = firebaseFirestore.collection("User").document(loggedUserID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    playingGames = (ArrayList<Integer>) document.get("playingGames");

                    //RICERCA GIOCHI IN LISTA
                    if (!playingGames.isEmpty()){
                        Integer gameID;
                        for (int j = 0; j < playingGames.size(); j++) {
                            gameID = Integer.parseInt(String.valueOf(playingGames.get(j)));

                            progressBar = requireView().findViewById(R.id.progressBar);
                            recyclerView = requireView().findViewById(R.id.playingRecyclerView);
                            recyclerDataArrayList = new ArrayList<>();

                            IGamesRepository iGamesRepository = new GamesRepository(getActivity().getApplication(),
                                    new ResponseCallback() {
                                        @Override
                                        public void onSuccess(List<GameApiResponse> gamesList, int count) {
                                            progressBar.setVisibility(View.GONE);
                                            for (GameApiResponse gameApiResponse : gamesList) {
                                                if (gameApiResponse.getCover() != null)
                                                    recyclerDataArrayList.add(new RecyclerData(gameApiResponse.getId(), gameApiResponse.getCover().getUrl()));
                                            }
                                            RecyclerProfileViewAdapter adapter=new RecyclerProfileViewAdapter(recyclerDataArrayList,getContext());
                                            GridLayoutManager layoutManager=new GridLayoutManager(getContext(),3);
                                            recyclerView.setLayoutManager(layoutManager);
                                            recyclerView.setAdapter(adapter);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {

                                        }
                                    });
                            progressBar.setVisibility(View.VISIBLE);
                            String query = "fields name, cover.url; where id = " + gameID + "; limit 30;";
                            iGamesRepository.fetchGames(query, 0);
                        }
                    } else {
                        recyclerView = requireView().findViewById(R.id.playingRecyclerView);
                        recyclerDataArrayList = new ArrayList<>();
                        RecyclerProfileViewAdapter adapter=new RecyclerProfileViewAdapter(recyclerDataArrayList,getContext());
                        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),3);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Log.d("LOGGER", "No such document");
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.getException());
            }
        });
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private boolean isLogged(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (firebaseAuth.getCurrentUser() == null && account == null) {
            return false;
        } else {
            if (firebaseAuth.getCurrentUser() != null) {
                loggedUserID = firebaseAuth.getCurrentUser().getUid();
            } else if (account != null) {
                loggedUserID = account.getId();
            }
            return true;
        }
    }
}