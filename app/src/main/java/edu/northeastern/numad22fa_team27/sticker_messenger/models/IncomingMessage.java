package edu.northeastern.numad22fa_team27.sticker_messenger.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class IncomingMessage implements Serializable {
    // date sticker was sent
    private Date dateSent;
    // user that sent the sticker
    private String sourceUser;
    // unique identifier for sticker
    private StickerTypes sticker;

    public IncomingMessage() {}

    public IncomingMessage(Date dateSent, String sourceUser, StickerTypes sticker) {
        this.setDateSent(dateSent);
        this.setSourceUser(sourceUser);
        this.setSticker(sticker);
    }

    public IncomingMessage(OutgoingMessage msg, String sourceUser) {
        this.setDateSent(msg.getDateSent());
        this.setSourceUser(sourceUser);
        this.setSticker(msg.getSticker());
    }


    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }

    public StickerTypes getSticker() {
        return sticker;
    }

    public void setSticker(StickerTypes sticker) {
        this.sticker = sticker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IncomingMessage other = (IncomingMessage) o;

        return other.sourceUser.equals(this.sourceUser)
                && other.sticker == this.sticker
                && other.dateSent.equals(this.dateSent);
    }
}
