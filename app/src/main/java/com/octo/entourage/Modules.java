package com.octo.entourage;

/**
 * Modules composing the app
 */
final class Modules {

    static Object[] list(EntourageApplication app) {
        return new Object[]{
                new EntourageModule(app)
        };
    }

    private Modules() {
        //not instanciables
    }
}
