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

import java.util.List;

import org.csstudio.scan.command.ScanCommand;
import org.csstudio.scan.command.ScanCommandFactory;
import org.csstudio.scan.command.XMLCommandReader;
import org.csstudio.scan.ecrscan.ui.model.AbstractScanTreeItem;
import org.csstudio.scan.ecrscan.ui.model.ModelTreeTable;
import org.csstudio.scan.ecrscan.ui.model.ScanServerItem;
import org.csstudio.scan.ecrscan.ui.model.ScanTreeModel;
import org.csstudio.ui.fx.util.FXEditorPart;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class ECRScanViewerEditor extends FXEditorPart implements IShellProvider {

    /** The editor ID */
    public static final String ID = "org.csstudio.scan.ecrscan.ui.editor.viewer";
    private final static ScanTreeModel inputModel = new ScanTreeModel();
    private final static ScanServerItem scanServerItem = new ScanServerItem("ecrscan");
    private final static ModelTreeTable<AbstractScanTreeItem<?>> model = new ModelTreeTable<AbstractScanTreeItem<?>>(
            scanServerItem,
            AbstractScanTreeItem::getItems,
            AbstractScanTreeItem::nameProperty,
            AbstractScanTreeItem::idProperty,
            AbstractScanTreeItem::finishedProperty,
            AbstractScanTreeItem::createdProperty,
            AbstractScanTreeItem::percentProperty,
            AbstractScanTreeItem::yformulaProperty,
            AbstractScanTreeItem::colorProperty,
            AbstractScanTreeItem::typeProperty,
            AbstractScanTreeItem::widthProperty,
            AbstractScanTreeItem::pointTypeProperty,
            AbstractScanTreeItem::pointSizeProperty,
            AbstractScanTreeItem::yaxisProperty,
            item -> PseudoClass.getPseudoClass(item.getClass().getSimpleName().toLowerCase()));

    /**
     * Constructs a new editor.
     */
    public ECRScanViewerEditor() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.ui.fx.util.FXEditorPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        final IEditorInput input = getEditorInput();
        final IFile file = (IFile) input.getAdapter(IFile.class);
        if (file != null)
        {
            try
            {
                final XMLCommandReader reader = new XMLCommandReader(new ScanCommandFactory());
                final List<ScanCommand> commands = reader.readXMLStream(file.getContents());
                inputModel.setCommands(commands);
            }
            catch (Exception ex)
            {
                MessageDialog.openError(parent.getShell(), Messages.Error,
                        NLS.bind(Messages.FileOpenErrorFmt,
                                new Object[] { input.getName(), ex.getMessage() }));
            }
        }
        super.createPartControl(parent);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName(input.getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        Scan.closeConnections();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.saverestore.ui.util.FXEditorPart#createFxScene()
     */
    @Override
    protected Scene createFxScene() {
        try {
            return Scan.createScene(inputModel, model);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new Scene(new BorderPane());
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.saverestore.ui.util.FXEditorPart#setFxFocus()
     */
    @Override
    public void setFxFocus() {

    }
    
    @Override
    public Shell getShell() {
        return getSite().getShell();
    }
}
