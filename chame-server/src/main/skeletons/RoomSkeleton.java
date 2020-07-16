package main.skeletons;

import java.util.List;

public class RoomSkeleton {
    private int id;
    private String name;
    private long createDate;
    private String owner;
    private boolean isPrivate;
    private List<UserSkeleton> memberList;
    private int unreadMessages = 0;

    public RoomSkeleton() {
    }

    public RoomSkeleton(int id, String name, long createDate, String owner, boolean isPrivate, List<UserSkeleton> memberList) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.owner = owner;
        this.isPrivate = isPrivate;
        this.memberList = memberList;
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
    }
}
