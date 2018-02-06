package uk.co.gencoreoperative.btw.ui.workers;

import static java.text.MessageFormat.*;
import static uk.co.gencoreoperative.btw.ui.panels.ProgressPanel.State.*;
import static uk.co.gencoreoperative.btw.utils.Timer.*;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import uk.co.gencoreoperative.btw.ActionFactory;
import uk.co.gencoreoperative.btw.PathResolver;
import uk.co.gencoreoperative.btw.utils.Logger;
import uk.co.gencoreoperative.btw.version.Version;
import uk.co.gencoreoperative.btw.version.VersionManager;
import uk.co.gencoreoperative.btw.ui.Context;
import uk.co.gencoreoperative.btw.ui.DialogFactory;
import uk.co.gencoreoperative.btw.ui.Errors;
import uk.co.gencoreoperative.btw.ui.panels.ProgressPanel;
import uk.co.gencoreoperative.btw.ui.signals.InstalledVersion;
import uk.co.gencoreoperative.btw.ui.signals.MinecraftHome;
import uk.co.gencoreoperative.btw.ui.signals.PatchFile;

/**
 * PatchWorker will handle the actual work of performing the patching process.
 *
 * Progress will be indicated directly to the {@link ProgressPanel}.
 */
public class PatchWorker extends SwingWorker<PatchWorker.Status, ProgressPanel.State> {
    private final PatchFile patchFile;
    private final ActionFactory factory;
    private final PathResolver pathResolver;
    private final Context context;
    private final ProgressPanel panel;
    private final DialogFactory dialogFactory;
    private VersionManager manager;

    public PatchWorker(MinecraftHome minecraftHome, PatchFile patchFile, ActionFactory factory, Context context, ProgressPanel panel, DialogFactory dialogFactory) {
        this.patchFile = patchFile;
        this.factory = factory;
        pathResolver = new PathResolver(minecraftHome.getFolder());
        this.context = context;
        this.panel = panel;
        this.dialogFactory = dialogFactory;
        manager = VersionManager.getVersionManager(pathResolver);
    }

    @Override
    protected Status doInBackground() throws Exception {
        // Read previous version - before folder is deleted.
        Optional<Version> previousVersion = manager.getVersion();

        // Run the Initialise Worker to setup the installation folder.
        publish(ProgressPanel.State.CLEAN_PREVIOUS_INSTALLATION);
        InitialiseWorker initialiseWorker = new InitialiseWorker(pathResolver);
        initialiseWorker.addPropertyChangeListener(evt -> panel.setProgress(initialiseWorker.getProgress()));
        initialiseWorker.execute();
        Status status = initialiseWorker.get();
        if (!status.isSuccess()) {
            Logger.error(status.getError());
            return status;
        }

        // Locate Client Jar
        publish(ProgressPanel.State.COPY_1_5_2);
        final LocateWorker worker = new LocateWorker(pathResolver);
        // Wire up workers progress to the ProgressPanel
        worker.addPropertyChangeListener(evt -> panel.setProgress(worker.getProgress()));
        worker.execute();
        final File clientJar;
        try {
            clientJar = worker.get();
        } catch (InterruptedException | ExecutionException e) {
            dialogFactory.failed(format(
                    "<html><b>Failed to locate the 1.5.2 client jar</b><br>{0}</html>",
                    e.getCause().getMessage()));
            return new Status();
        }

        // Write JSON
        publish(COPY_JSON);
        File json = factory.copyJsonToInstallation(pathResolver);
        if (!json.exists()) {
            return new Status(format("{0}\n{1}",
                    Errors.FAILED_TO_WRITE_JSON.getReason(),
                    json.getAbsolutePath()));
        }

        // Create the Better Than Wolves Jar
        publish(CREATE_JAR);
        final ActionFactory.MonitoredSet monitoredSet = factory.mergeClientWithPatch(clientJar, patchFile.getFile());
        monitoredSet.addObserver((o, arg) -> setProgress(monitoredSet.getProgress()));
        File jar = timeAndReturn("Creating Jar", () -> factory.writeToTarget(pathResolver, monitoredSet));

        // Write the version to the installation folder
        publish(WRITE_VERSION);
        Version version = VersionManager.createVersion(patchFile);
        manager.save(version);

        // Signal to the application that BTW has been installed
        publish(COMPLETE);
        InstalledVersion installedVersion = new InstalledVersion(jar, version);
        context.add(installedVersion);

        return new Status();
    }

    @Override
    protected void process(List<ProgressPanel.State> chunks) {
        chunks.forEach(panel::setState);
    }

    public static class Status {
        private final boolean success;
        private final String error;

        public Status(String error) {
            success = false;
            this.error = error;
        }

        public Status() {
            success = true;
            error = null;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}
