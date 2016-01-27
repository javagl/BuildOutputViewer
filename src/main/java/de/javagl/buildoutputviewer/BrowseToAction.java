/*
 * www.javagl.de - BuildOutputViewer
 *
 * Copyright (c) 2016 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.buildoutputviewer;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.JTable;

import de.javagl.common.ui.LocationBasedAction;

/**
 * A location based action, to be attached to a JTable, that obtains a 
 * value from a certain column in the table, uses this value to obtain
 * a link, and browses to this link when it is executed
 */
class BrowseToAction extends LocationBasedAction
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BrowseToAction.class.getName());
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -216441085618613845L;
    
    /**
     * The column that contains the key value that will be used for
     * looking up the link in the {@link #linkLookup}
     */
    private final int keyColumnIndex;

    /**
     * The function that will be fed with the value from the table,
     * and return a link to browse to
     */
    private final Function<Object, String> linkLookup;
    
    /**
     * The link that will be opened
     */
    private String link;


    /**
     * Creates a new action that looks up the value in the specified
     * column in the table and passes it to the given function to
     * obtain the link that should be browsed to when the action
     * is executed
     * 
     * @param keyColumnIndex The column index for the lookup key
     * @param linkLookup The link lookup function
     */
    BrowseToAction(int keyColumnIndex, Function<Object, String> linkLookup)
    {
        this.keyColumnIndex = keyColumnIndex;
        this.linkLookup = linkLookup;
    }

    @Override
    protected void prepareShow(Component component, int x, int y)
    {
        Point p = new Point(x,y);
        JTable table = (JTable)component;
        int viewRow = table.rowAtPoint(p);
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = table.getModel().getValueAt(
            modelRow, keyColumnIndex);
        link = linkLookup.apply(value);
        if (link == null)
        {
            setEnabled(false);
            putValue(NAME, 
                "No link found for "+value);
        }
        else
        {
            setEnabled(true);
            putValue(NAME, 
                "Lookup code "+value+" at "+link);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            Desktop.getDesktop().browse(new URI(link));
        }
        catch (IOException ex)
        {
            logger.warning(ex.getMessage());
        }
        catch (URISyntaxException ex)
        {
            logger.warning(ex.getMessage());
        }
    }
}