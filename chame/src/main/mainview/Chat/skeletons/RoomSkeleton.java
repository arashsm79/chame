package main.mainview.Chat.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import main.mainview.Game.skeletons.GameRoomSkeleton;
import main.connection.skeletons.UserSkeleton;

import java.util.List;

public class RoomSkeleton {
    private int id;
    private String name;
    private long createDate;
    private String owner;
    private boolean isPrivate;
    private List<UserSkeleton> memberList;
    private int unreadMessages = 0;

    private transient StringProperty unreadMessagesString = new SimpleStringProperty();

    private ObservableList<ChatMessageSkeleton> chatsObservableList;
    private ObservableList<GameRoomSkeleton> gameLobbiesObservableList;

    public RoomSkeleton() {
    }

    public RoomSkeleton(int id, String name, long createDate, String owner, boolean isPrivate, List<UserSkeleton> memberList) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.owner = owner;
        this.isPrivate = isPrivate;
        this.memberList = memberList;

        unreadMessagesString.setValue(Integer.toString(unreadMessages));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<UserSkeleton> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<UserSkeleton> memberList) {
        this.memberList = memberList;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
        this.unreadMessagesString.setValue(Integer.toString(unreadMessages));
    }

    public StringProperty getUnreadMessagesString() {
        return unreadMessagesString;
    }

    public StringProperty unreadMessagesStringProperty() {
        return unreadMessagesString;
    }

    public void setUnreadMessagesString(String unreadMessagesString) {
        this.unreadMessagesString.set(unreadMessagesString);
    }

    public ObservableList<ChatMessageSkeleton> getChatsObservableList() {
        return chatsObservableList;
    }

    public void setChatsObservableList(ObservableList<ChatMessageSkeleton> chatsObservableList) {
        this.chatsObservableList = chatsObservableList;
    }

    public ObservableList<GameRoomSkeleton> getGameLobbiesObservableList() {
        return gameLobbiesObservableList;
    }

    public void setGameLobbiesObservableList(ObservableList<GameRoomSkeleton> gameLobbiesObservableList) {
        this.gameLobbiesObservableList = gameLobbiesObservableList;
    }
}
