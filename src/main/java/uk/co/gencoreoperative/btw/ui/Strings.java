package uk.co.gencoreoperative.btw.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Defines the strings that are used for display purposes in the application.
 */
public enum Strings {
    TITLE("Patch Installer"),
    VERSION(readProperty("/app.version")),
    BUTTON_CLOSE("Close"),
    BUTTON_SELECT("Select"),
    BUTTON_PATCH("Patch"),
    SELECT_ZIP_TITLE("Select BetterThanWolves Zip"),
    SELECT_MC_HOME("Select Minecraft Installation");

    private final String text;

    Strings(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    private static String readProperty(String path) {
        InputStream stream = Strings.class.getResourceAsStream(path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
