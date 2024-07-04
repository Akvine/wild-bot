package ru.akvine.marketspace.bot.constants;

public final class ClientStates {

    private ClientStates() {
        throw new IllegalStateException("Calling ClientStates constructor is prohibited!");
    }

    public interface START_ADVERT {
        String CHOOSE_CATEGORY_STATE = "CHOOSE_CATEGORY";
        String UPLOAD_PHOTO_STATE = "UPLOAD_PHOTO";
    }
}
