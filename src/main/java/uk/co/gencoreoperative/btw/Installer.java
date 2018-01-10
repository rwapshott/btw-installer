/*
 * Copyright 2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package uk.co.gencoreoperative.btw;

import static uk.co.gencoreoperative.btw.ui.Strings.*;

import java.util.Set;

import com.beust.jcommander.Parameter;
import uk.co.gencoreoperative.btw.command.AbstractCommand;
import uk.co.gencoreoperative.btw.ui.DialogFactory;
import uk.co.gencoreoperative.btw.ui.Progress;
import uk.co.gencoreoperative.btw.ui.Strings;

/**
 * Describes the configuration and resolution of dependencies based on the
 * provided configuration.
 */
public class Installer {
    @Parameter(names = {"-r", "--remove"}, description = "Removes the BetterThanWolves version from Minecraft")
    private boolean remove = false;

    @Parameter(names = {"-s", "--serverMode"}, description = "Server mode, install BetterThanWovles mod to Minecraft Server")
    private boolean serverMode = false;

    @Parameter(names = {"-h", "--help"}, description = "This help information")
    private boolean help = false;

    private final Commands commands;
    private final Progress progress;

    /**
     * Creates a default Installer instance, which has been given a UI component
     * to use as the parent component for all dialogs.
     */
    public Installer() {
        progress = new Progress();
        commands = new Commands(new ActionFactory(new DialogFactory(progress)));
    }

    public Set<AbstractCommand> getCommands() {
        if (remove) return commands.getClientRemoveCommands();
        return commands.getClientCommands();
    }

    public Progress getUserInterface() {
        progress.setTitle(remove ? TITLE_REMOVE.getText() : TITLE_PATCH.getText());
        progress.setMainAction(remove ? BUTTON_REMOVE.getText() : BUTTON_PATCH.getText());
        progress.setSuccessMessage(
                remove ? TITLE_REMOVE_SUCCESS.getText() : TITLE_PATCH_SUCCESS.getText(),
                remove ? MSG_REMOVE_SUCCESS.getText() : MSG_PATCH_SUCCESS.getText());
        return progress;
    }

    public boolean isHelp() {
        return help;
    }
}