package main;
import main.skeletons.GameRoomSkeleton;
import main.skeletons.RoomSkeleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//this singleton class manages real time interaction like chat
public class OnlineRoomsManager {


    //a map of all the active rooms
    public ConcurrentHashMap<Integer, Set<ClientSession>> roomsList = new ConcurrentHashMap<>();
    private static OnlineRoomsManager onlineRoomsManager_instance = null;
    private OnlineRoomsManager(){

    }

    public void addOnlineUser(ClientSession clientSession, ArrayList<RoomSkeleton> clientRoomList){
        if(clientSession == null || clientRoomList == null || clientRoomList.size() == 0)
            return;
        synchronized (clientRoomList){
            Iterator<RoomSkeleton> it = clientRoomList.iterator();
            while (it.hasNext()){
                RoomSkeleton rs = it.next();
                roomsList.computeIfPresent(rs.getId(), (key, val)->{
                    val.add(clientSession);
                    return val;
                });
                roomsList.computeIfAbsent(rs.getId(), k -> Collections.synchronizedSet(new HashSet<>()))
                        .add(clientSession);

                clientSession.addRoomId(rs.getId());
            }
        }

    }

    //return the instance
    public static OnlineRoomsManager getInstance() {
        if(onlineRoomsManager_instance ==null)
            onlineRoomsManager_instance = new OnlineRoomsManager();
        return onlineRoomsManager_instance;
    }
}
