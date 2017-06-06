package com.zenval.translatefiles.service;

public class TranslationException extends Exception {

    private boolean shutdown;

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, boolean shutdown) {
        super(message);
        this.shutdown = shutdown;
    }

    public boolean shouldShutdown(){
        return shutdown;
    }
}
