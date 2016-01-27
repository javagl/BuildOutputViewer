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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A dialog where build output may be entered in a text area via copy & paste
 */
class BuildLogInputDialog extends JDialog
{
    /**
     * Serial UID 
     */
    private static final long serialVersionUID = 5945703949601087400L;
    
    /**
     * The text area
     */
    private final JTextArea textArea;
    
    /**
     * The option that was chosen, either JOptionPane.CANCEL_OPTION or
     * JOptionPane.OK_OPTION  
     */
    private int option = JOptionPane.CANCEL_OPTION;
    
    /**
     * Create the dialog with the given parent
     * 
     * @param parent The parent
     */
    BuildLogInputDialog(Window parent)
    {
        super(parent);
        setTitle("Build log input");
        setModal(true);
        
        getContentPane().setLayout(new BorderLayout());
        
        JLabel infoLabel = new JLabel("Copy and paste that mess here:");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        getContentPane().add(infoLabel, BorderLayout.NORTH);
        textArea = new JTextArea(40, 100);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> 
        {
            option = JOptionPane.OK_OPTION;
            setVisible(false);   
        });
        buttonsPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> 
        {
            option = JOptionPane.CANCEL_OPTION;
            setVisible(false);   
        });
        buttonsPanel.add(cancelButton);
        
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Returns the option indicating how this dialog was closed:
     * Either JOptionPane.CANCEL_OPTION or JOptionPane.OK_OPTION  
     * 
     * @return The option
     */
    int getOption()
    {
        return option;
    }
    
    /**
     * Obtain the text from the text area
     *  
     * @return The text
     */
    String getText()
    {
        return textArea.getText();
    }
}