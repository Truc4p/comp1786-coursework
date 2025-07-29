package com.example.yoga_admin_app;

public class PendingDeletion {
    private long id;
    private String itemType; // "class" or "instance"
    private long itemId;
    private String cloudId;
    private long timestamp;

    // Constructors
    public PendingDeletion() {}

    public PendingDeletion(String itemType, long itemId, String cloudId, long timestamp) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.cloudId = cloudId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PendingDeletion{" +
                "id=" + id +
                ", itemType='" + itemType + '\'' +
                ", itemId=" + itemId +
                ", cloudId='" + cloudId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
