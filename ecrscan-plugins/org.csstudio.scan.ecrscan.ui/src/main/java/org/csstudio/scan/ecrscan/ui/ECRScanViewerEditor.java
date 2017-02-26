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

import org.csstudio.ui.fx.util.FXEditorPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


public class ECRScanViewerEditor extends FXEditorPart {

    /** The editor ID */
    public static final String ID = "org.csstudio.scan.ecrscan.ui.editor.viewer";

    private final ScanController controller;

    /**
     * Constructs a new editor.
     */
    public ECRScanViewerEditor() {
        controller = new ScanController();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.ui.fx.util.FXEditorPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }

    private void init() {

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
            return Scan.createScene();
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
}
