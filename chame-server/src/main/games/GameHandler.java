package main.games;

import main.skeletons.GameRoomSkeleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//a singleton class ofr handling games
public class GameHandler {

    //A handler for the games in the server

    //a simple id generator for each game lobby (game room)
    private AtomicInteger gameRoomIdGenerator = new AtomicInteger(0);

    //a map that holds lobbies associated with each room
    public ConcurrentHashMap<Integer, Set<GameRoomSkeleton>> gameList = new ConcurrentHashMap<>();
    private static GameHandler gameHandler_instance = null;
    private GameHandler(){

    }

    //add a game to the map
    public void addGame(GameRoomSkeleton gameRoom){
        if(gameRoom == null)
            return;

        gameList.computeIfAbsent(gameRoom.getRoomID(), k -> Collections.synchronizedSet(new HashSet<>()))
                .add(gameRoom.setGameRoomID(gameRoomIdGenerator.incrementAndGet()));

    }

    //removes a game from the map
    public void removeGame(GameRoomSkeleton gameRoom){
        if(gameRoom == null)
            return;

        gameList.search(1, (k, v) ->{
            if(k == gameRoom.getRoomID()){
                synchronized (v){
                    Iterator<GameRoomSkeleton> it = v.iterator();
                    while(it.hasNext()){
                        GameRoomSkeleton grs = it.next();
                        if(gameRoom.getGameRoomID() == grs.getGameRoomID()){
                            it.remove();
                            return null;
                        }
                    }
                }
            }
            return null;
        });


    }

    //returns the instance
    public static GameHandler getInstance() {
        if(gameHandler_instance==null)
            gameHandler_instance = new GameHandler();

        return gameHandler_instance;
    }
}
