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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

/**
 * Utility methods
 */
class Utils
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(Utils.class.getName());
    
    /**
     * Try to parse an Integer from the given string. If the string can
     * not be parsed (after trimming it), then <code>null</code> will 
     * be returned
     * 
     * @param string The string
     * @return The parsed integer
     */
    static Integer tryParseInt(String string)
    {
        try
        {
            return Integer.parseInt(string.trim(), 10);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Normalize the given path. The given string is trimmed and converted
     * into a file, and the string representation of this file is returned.
     * This normalizes different (possibly mixed) path separators that may
     * appear in the given string.
     * 
     * @param path The path
     * @return The normalized path
     */
    static String normalizePath(String path)
    {
        return new File(path.trim()).toString();
    }
    
    /**
     * Create an uneditable default table model with the given column names
     *  
     * @param columnNames The column names
     * @return The table model
     */
    static DefaultTableModel createUneditableTableModel(
        String ... columnNames)
    {
        return new DefaultTableModel(columnNames, 0)  
        {
            /**
             * Serial UID
             */
            private static final long serialVersionUID = 6490129562872132785L;

            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return false;
            }
        };
    }
    
    /**
     * Creates an uneditable text field that looks like a label, but 
     * allows selecting the text for copy & paste
     * 
     * @return The text field
     */
    static JTextField createLabelLikeTextField()
    {
        JTextField textField = new JTextField()
        {
            /**
             * Serial UID 
             */
            private static final long serialVersionUID = -1559517467869034292L;

            @Override
            public Color getBackground() 
            {
                if (getParent() != null)
                {
                    return getParent().getBackground();
                }
                return super.getBackground();
            }
        };
        textField.setBorder(null);
        textField.setEditable(false);
        return textField;
    }
    
    /**
     * Create a text field for displaying the given file name. This is a 
     * text field that is not editable and looks like a label, but allows 
     * selecting the displayed text (for copy and paste), and opening a 
     * popup menu that allows opening the file itself, or the folder 
     * that contains the file with the given name
     * 
     * @param fileName The file name 
     * @return The component
     */
    static JTextField createFileTextField(String fileName)
    {
        JTextField textField = createLabelLikeTextField();
        textField.setText(fileName);
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem openFileMenuItem = 
            new JMenuItem("Open...");
        JMenuItem openContainingFolderMenuItem = 
            new JMenuItem("Open containing folder...");
        popupMenu.addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                File file = new File(textField.getText());
                openFileMenuItem.setEnabled(file.exists());
                File directory = file.getParentFile();
                openContainingFolderMenuItem.setEnabled(directory.exists());
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                // Ignored
            }
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e)
            {
                // Ignored
            }
        });
        
        openFileMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    File file = new File(textField.getText());
                    Desktop.getDesktop().open(file);
                }
                catch (IOException ex)
                {
                    logger.warning(ex.getMessage());
                }
            }
        });
        openContainingFolderMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    File file = new File(textField.getText());
                    File directory = file.getParentFile();
                    Desktop.getDesktop().open(directory);
                }
                catch (IOException ex)
                {
                    logger.warning(ex.getMessage());
                }
            }
        });
        popupMenu.add(openFileMenuItem);
        popupMenu.add(openContainingFolderMenuItem);
        textField.setComponentPopupMenu(popupMenu);
        textField.add(popupMenu);
        return textField;
        
    }
    

    /**
     * Private constructor to prevent instantiation
     */
    private Utils()
    {
        // Private constructor to prevent instantiation
    }

}
