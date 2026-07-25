package com.foodbridge.dto;

public class ChatMessageDTO {
    private Long id;
    private Long listingId;
    private Long senderId;
    private String senderName;
    private String senderRole; // DONOR or NGO
    private String message;
    private boolean isRead;
    private String sentAt;
    private String type; // e.g., CHAT, READ_RECEIPT

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(Long id, Long listingId, Long senderId, String senderName, String senderRole, String message, boolean isRead, String sentAt, String type) {
        this.id = id;
        this.listingId = listingId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.isRead = isRead;
        this.sentAt = sentAt;
        this.type = type != null ? type : "CHAT";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean getIsRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public static ChatMessageDTOBuilder builder() {
        return new ChatMessageDTOBuilder();
    }

    public static class ChatMessageDTOBuilder {
        private Long id;
        private Long listingId;
        private Long senderId;
        private String senderName;
        private String senderRole;
        private String message;
        private boolean isRead = false;
        private String sentAt;
        private String type = "CHAT";

        ChatMessageDTOBuilder() {}

        public ChatMessageDTOBuilder id(Long id) { this.id = id; return this; }
        public ChatMessageDTOBuilder listingId(Long listingId) { this.listingId = listingId; return this; }
        public ChatMessageDTOBuilder senderId(Long senderId) { this.senderId = senderId; return this; }
        public ChatMessageDTOBuilder senderName(String senderName) { this.senderName = senderName; return this; }
        public ChatMessageDTOBuilder senderRole(String senderRole) { this.senderRole = senderRole; return this; }
        public ChatMessageDTOBuilder message(String message) { this.message = message; return this; }
        public ChatMessageDTOBuilder isRead(boolean isRead) { this.isRead = isRead; return this; }
        public ChatMessageDTOBuilder sentAt(String sentAt) { this.sentAt = sentAt; return this; }
        public ChatMessageDTOBuilder type(String type) { this.type = type; return this; }

        public ChatMessageDTO build() {
            return new ChatMessageDTO(id, listingId, senderId, senderName, senderRole, message, isRead, sentAt, type);
        }
    }
}
