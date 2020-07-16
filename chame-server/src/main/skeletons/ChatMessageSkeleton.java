package main.skeletons;

public class ChatMessageSkeleton {
    private int id;
    private String body;
    private String senderUsername;
    private int room_id;
    private String type;
    private String roomName;
    private boolean read;
    private long createDate;

    public ChatMessageSkeleton() {
    }

    public ChatMessageSkeleton(int id, String body, String senderUsername, int room_id, String type, String roomName, boolean read, long createDate) {
        this.id = id;
        this.body = body;
        this.senderUsername = senderUsername;
        this.room_id = room_id;
        this.type = type;
        this.roomName = roomName;
        this.read = read;
        this.createDate = createDate;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
