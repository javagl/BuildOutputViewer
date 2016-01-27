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
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JTable;

import de.javagl.common.ui.LocationBasedAction;

/**
 * A location based action, to be attached to a JTable, that allows
 * opening the a file whose name is found in a certain column in 
 * the table
 */
class OpenFileAction extends LocationBasedAction
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(OpenFileAction.class.getName());
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -216441085618613845L;
    
    /**
     * The column containing the file name
     */
    private final int fileNameColumnIndex;
    
    /**
     * The file that will be opened
     */
    private File file;

    /**
     * Creates a new action
     * 
     * @param fileNameColumnIndex The column containing the file name
     */
    OpenFileAction(int fileNameColumnIndex)
    {
        this.fileNameColumnIndex = fileNameColumnIndex;
    }
    
    @Override
    protected void prepareShow(Component component, int x, int y)
    {
        Point p = new Point(x,y);
        JTable table = (JTable)component;
        int viewRow = table.rowAtPoint(p);
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = table.getModel().getValueAt(
            modelRow, fileNameColumnIndex);
        String fileName = String.valueOf(value);
        file = new File(fileName);
        if (!file.exists())
        {
            setEnabled(false);
            putValue(NAME, 
                "File not found: "+fileName);
        }
        else
        {
            setEnabled(true);
            putValue(NAME, 
                "Open "+file.getName());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            Desktop.getDesktop().open(file);
        }
        catch (IOException ex)
        {
            logger.warning(ex.getMessage());
        }
    }
}