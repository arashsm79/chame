package main.mainview.Chat.skeletons;

import java.util.ArrayList;

public class RoomMessageResponseSkeleton {
    private int room_id;
    private ArrayList<ChatMessageSkeleton> roomMessageList = new ArrayList<>();

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public ArrayList<ChatMessageSkeleton> getRoomMessageList() {
        return roomMessageList;
    }

    public void setRoomMessageList(ArrayList<ChatMessageSkeleton> roomMessageList) {
        this.roomMessageList = roomMessageList;
    }
}
