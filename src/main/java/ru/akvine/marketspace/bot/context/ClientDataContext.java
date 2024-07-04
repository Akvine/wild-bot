package ru.akvine.marketspace.bot.context;

import org.jetbrains.annotations.Nullable;

public class ClientDataContext {
    private static final ThreadLocal<ClientData> CLIENT_DATA_CONTEXT = new InheritableThreadLocal<>();

    @Nullable
    public static ClientData get() {
        return CLIENT_DATA_CONTEXT.get();
    }

    public static void set(ClientData clientData) {
        CLIENT_DATA_CONTEXT.set(clientData);
    }
}
