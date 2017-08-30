package uk.co.gencoreoperative.btw;

import java.io.File;
import java.util.function.Supplier;

/**
 * Responsible for providing the platform dependent location of Minecraft
 */
public class MineCraftPathResolver implements Supplier<File> {
    private String path;

    public MineCraftPathResolver(String path) {
        this.path = path;
    }

    public MineCraftPathResolver() {
        this("/Users/robert.wapshott/Library/Application Support/minecraft");
    }

    @Override
    public File get() {
        return new File(path);
    }

    public File versions() {
        return new File(get(), "versions");
    }

    public File oneFiveTwo() {
        return new File(versions(), "1.5.2");
    }

    public File betterThanWolves() {
        return new File(versions(), "BetterThanWolves");
    }
}