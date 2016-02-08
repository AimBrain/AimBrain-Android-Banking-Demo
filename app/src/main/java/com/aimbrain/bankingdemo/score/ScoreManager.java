package com.aimbrain.bankingdemo.score;

import com.aimbrain.sdk.models.ScoreModel;

import java.util.HashSet;
import java.util.Set;


public class ScoreManager {

    static ScoreManager instance;
    private Set<ScoreListener> listenersList;
    private long timestamp;
    private ScoreModel scoreModel;

    public static ScoreManager getInstance(){
        if(instance == null)
            instance = new ScoreManager();
        return instance;
    }

    private ScoreManager(){
        listenersList = new HashSet<>();
    }

    public void registerListener(ScoreListener scoreListener){
        listenersList.add(scoreListener);
        if(scoreModel != null)
            scoreListener.updateScore(scoreModel, timestamp);
    }

    public void unregisterListener(ScoreListener scoreListener){
        if(listenersList.contains(scoreListener))
            listenersList.remove(scoreListener);
    }

    public void scoreChanged(ScoreModel scoreModel, long timestamp){
        this.scoreModel = scoreModel;
        this.timestamp = timestamp;
        for(ScoreListener listener : listenersList){
            listener.updateScore(scoreModel, timestamp);
        }
    }
    public void clearCache(){
        this.scoreModel = null;
    }

}
