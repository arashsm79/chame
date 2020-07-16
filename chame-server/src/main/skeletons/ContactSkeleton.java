package main.skeletons;

public class ContactSkeleton {
    private int relationID;
    private String username;
    private String relationType;
    private boolean online;

    public ContactSkeleton(){};
    public ContactSkeleton(String username, String relationType, int relationID, boolean online) {
        this.username = username;
        this.relationType = relationType;
        this.relationID = relationID;
        this.online = online;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public boolean isOnline() {
        return online;
    }

    public int getRelationID() {
        return relationID;
    }

    public void setRelationID(int relationID) {
        this.relationID = relationID;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
