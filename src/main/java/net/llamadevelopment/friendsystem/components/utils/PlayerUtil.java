package net.llamadevelopment.friendsystem.components.utils;

public class PlayerUtil {

    private boolean requests;
    private boolean notifications;

    public PlayerUtil(boolean requests, boolean notifications) {
        this.requests = requests;
        this.notifications = notifications;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public boolean isRequests() {
        return requests;
    }
}
