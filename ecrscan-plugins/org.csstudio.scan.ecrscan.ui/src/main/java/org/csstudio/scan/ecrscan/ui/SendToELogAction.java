/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2016.
 *
 * Contact Information:
 *   Facility for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 */
package org.csstudio.scan.ecrscan.ui;

import java.util.Arrays;

import org.csstudio.logbook.LogEntryBuilder;
import org.csstudio.logbook.LogbookClientManager;
import org.csstudio.ui.fx.util.FXMessageDialog;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * <code>SendToELogAction</code> implements an action that creates the default content based on the selected snapshot
 * and forwards that content to the logbook entry dialog.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class SendToELogAction extends Action {
    private static final String ID = "org.csstudio.scan.ecrscan.ui.sendtoelog";
    private static final String LOGBOOK_UI_PLUGIN_ID = "org.csstudio.logbook.ui";
    private static final String OPEN_LOGENTRY_BUILDER_DIALOG_ID = "org.csstudio.logbook.ui.OpenLogEntryBuilderDialog";


    private Shell shell;

    /**
     * Construct a new action
     *
     * @param shell the parent shell (used for dialog display only)
     */
    public SendToELogAction(Shell shell) {
        this.shell = shell;
        setText("Create Log Entry");
        setImageDescriptor(CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(LOGBOOK_UI_PLUGIN_ID,
            "icons/logentry-add-16.png"));
        setId(ID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            final StringBuilder sb = new StringBuilder(1000);
            sb.append("ECR Scan\n");
            LogEntryBuilder entry = LogEntryBuilder.withText(sb.toString());
               // .attach(AttachmentBuilder.attachment(fileAttachment));
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Event event = new Event();
            event.data = Arrays.asList(entry);
            IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
            handlerService.executeCommand(OPEN_LOGENTRY_BUILDER_DIALOG_ID, event);
        } catch (Exception e) {
            FXMessageDialog.openError(shell, "Logbook Error", "Failed to make logbook entry: \n" + e.getMessage());
        }
    }

    /**
     * Checks if the logbook functionality is present in the application.
     * @return true if logbook is available or false otherwise
     */
    public static boolean isElogAvailable() {
        try {
            if (LogbookClientManager.getLogbookClientFactory() == null) {
                return false;
            }
            ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getService(ICommandService.class);
            Command command = commandService.getCommand(OPEN_LOGENTRY_BUILDER_DIALOG_ID);
            return command != null;
        } catch (Exception e) {
            return false;
        }
    }

}
