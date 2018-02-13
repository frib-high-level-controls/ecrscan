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


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 *
 * <code>Activator</code> is the bundle activator.
 *
 * @author <a href="mailto:berryman@frib.msu.edu">Eric Berryman</a>
 *
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.csstudio.scan.ecrscan.ui"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        setPlugin(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        setPlugin(this);
        super.stop(context);
    }

    /** Static setter to avoid FindBugs warning */
    private static void setPlugin(final Activator the_plugin)
    {
        plugin = the_plugin;
    }

    /** @return The shared instance. */
    public static Activator getDefault()
    {
        return plugin;
    }
}