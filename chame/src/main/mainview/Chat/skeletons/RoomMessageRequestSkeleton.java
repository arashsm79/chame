package main.mainview.Chat.skeletons;

public class RoomMessageRequestSkeleton {
    private RoomSkeleton roomSkeleton;
    private long afterTime;

    public RoomMessageRequestSkeleton() {
    }

    public RoomMessageRequestSkeleton(RoomSkeleton roomSkeleton, long afterTime) {
        this.roomSkeleton = roomSkeleton;
        this.afterTime = afterTime;
    }

    public RoomSkeleton getRoomSkeleton() {
        return roomSkeleton;
    }

    public void setRoomSkeleton(RoomSkeleton roomSkeleton) {
        this.roomSkeleton = roomSkeleton;
    }

    public long getAfterTime() {
        return afterTime;
    }

    public void setAfterTime(long afterTime) {
        this.afterTime = afterTime;
    }
}
