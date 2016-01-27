package de.javagl.buildoutputviewer;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.javagl.common.ui.GridBagLayouts;

/**
 * A component that can show information about a single {@link CompilerMessage}
 */
class CompilerMessagePanel extends JPanel
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1046477091580261681L;
    
    /**
     * The text field for the file name
     */
    private final JTextField fileNameTextField;
    
    /**
     * The text field for the line number
     */
    private final JTextField lineTextField;
    
    /**
     * The text field for the error/warning code
     */
    private final JTextField codeTextField;
    
    /**
     * The text field for the (short, single-lined) message
     */
    private final JTextField messageTextField;
    
    /**
     * The text area for the full message
     */
    private final JTextArea fullMessageTextArea;

    /**
     * Default constructor
     */
    CompilerMessagePanel()
    {
        super(new BorderLayout());
        
        this.fileNameTextField = Utils.createFileTextField("");
        this.lineTextField = Utils.createLabelLikeTextField();
        this.codeTextField = Utils.createLabelLikeTextField();
        this.messageTextField = Utils.createLabelLikeTextField();
        
        this.fullMessageTextArea = new JTextArea();
        this.fullMessageTextArea.setEditable(false);
        
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        int row = 0;
        GridBagLayouts.addRow(summaryPanel, row++, 1, 
            new JLabel("File:"), fileNameTextField);
        GridBagLayouts.addRow(summaryPanel, row++, 1, 
            new JLabel("Line:"), lineTextField);
        GridBagLayouts.addRow(summaryPanel, row++, 1, 
            new JLabel("Code:"), codeTextField);
        GridBagLayouts.addRow(summaryPanel, row++, 1, 
            new JLabel("Message:"), messageTextField);
        
        add(summaryPanel, BorderLayout.NORTH);
        
        add(new JScrollPane(fullMessageTextArea), BorderLayout.CENTER);
    }
    
    /**
     * Set the {@link CompilerMessage} to be displayed in this panel
     * 
     * @param compilerMessage The {@link CompilerMessage}
     */
    void setCompilerMessage(CompilerMessage compilerMessage)
    {
        if (compilerMessage == null)
        {
            fileNameTextField.setText(" ");
            lineTextField.setText(" ");
            codeTextField.setText(" ");
            messageTextField.setText(" ");
            fullMessageTextArea.setText(" ");
        }
        else
        {
            fileNameTextField.setText(
                compilerMessage.getNormalizedFileName());
            lineTextField.setText(
                String.valueOf(compilerMessage.getLineNumber()));
            codeTextField.setText(
                String.valueOf(compilerMessage.getCode()));
            messageTextField.setText(
                compilerMessage.getMessage());
            
            StringBuilder sb = new StringBuilder();
            for (String line : compilerMessage.getLinePayloads())
            {
                sb.append(line).append("\n");
            }
            fullMessageTextArea.setText(sb.toString());
        }
    }
}