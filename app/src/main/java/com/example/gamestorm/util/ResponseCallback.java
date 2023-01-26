package com.example.gamestorm.util;

import com.example.gamestorm.model.GameApiResponse;

import java.util.List;

public interface ResponseCallback {
    void onSuccess(List<GameApiResponse> gamesList, long lastUpdate);
    void onFailure(String errorMessage);
    void onGameFavoriteStatusChanged(GameApiResponse game);
}
