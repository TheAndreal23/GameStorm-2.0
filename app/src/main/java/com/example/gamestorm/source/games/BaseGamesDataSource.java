package com.example.gamestorm.source.games;

import androidx.lifecycle.MutableLiveData;

import com.example.gamestorm.model.GameApiResponse;

import java.util.List;

public abstract class BaseGamesDataSource {
    protected GameCallback gameCallback;

    public void setGameCallback(GameCallback gameCallback) {
        this.gameCallback = gameCallback;
    }
    public abstract void getGames(String query);
    public abstract void getGame(int id);
    public abstract void getPopularGames();
    public abstract void getBestGames();
    public abstract void getLatestGames();
    public abstract void getIncomingGames();
    public abstract void getExploreGames();
    public abstract void getForYouGames(String genre, int size);
    public abstract void getCompanyGames(String company);
    public abstract void getFranchiseGames(String franchise);
    public abstract void getGenreGames(String genre);
    public abstract void getSearchedGames(String userInput);
    public abstract void getSimilarGames(List<Integer> similarGames);
    public abstract void getAllPopularGames();
    public abstract void getAllBestGames();
    public abstract void getAllLatestGames();
    public abstract void getAllIncomingGames();
}
