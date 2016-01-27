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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import de.javagl.common.ui.GridBagLayouts;
import de.javagl.common.ui.JTables;
import de.javagl.common.ui.JTrees;
import de.javagl.common.ui.LocationBasedPopupHandler;

/**
 * A panel showing information about a single {@link BuildOutput}
 */
class BuildOutputPanel extends JPanel
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8297104402864096156L;

    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BuildOutputPanel.class.getName());
    
    /**
     * The {@link BuildOutput} that is shown in this panel
     */
    private final BuildOutput buildOutput;
    
    /**
     * The list of unique compiler warnings that was created from
     * the list in the {@link BuildOutput}
     */
    private final List<CompilerMessage> uniqueCompilerWarnings;
    
    /**
     * The list of unique compiler errors that was created from
     * the list in the {@link BuildOutput}
     */
    private final List<CompilerMessage> uniqueCompilerErrors;
    
    /**
     * The list of unique linker warnings that was created from
     * the list in the {@link BuildOutput}
     */
    private final List<LinkerMessage> uniqueLinkerWarnings;
    
    /**
     * The list of unique linker errors that was created from
     * the list in the {@link BuildOutput}
     */
    private final List<LinkerMessage> uniqueLinkerErrors;
    
    /**
     * A panel for showing a {@link CompilerMessage}
     */
    private CompilerMessagePanel compilerMessagePanel;
    
    /**
     * The {@link UniqueListSelectionHandler} for the tables showing
     * the compiler- and linker warnings and errors
     */
    private final UniqueListSelectionHandler uniqueListSelectionHandler;
        
    /**
     * The tree showing the "reverse" includes. That is, for the file of 
     * the selected {@link CompilerMessage}, this tree will show all 
     * the files that include THIS file
     */
    private JTree reverseIncludesTree;
    
    /**
     * The tree showing the "forward" includes. That is, for the file of 
     * the selected {@link CompilerMessage}, this tree will show all 
     * the files that are included by THIS file
     */
    private JTree forwardIncludesTree;
    
    /**
     * The text area showing the leaf nodes of the reverse include tree
     */
    private JTextArea reverseTreeLeavesTextArea;
    
    /**
     * Creates a panel showing the given {@link BuildOutput}
     * 
     * @param buildOutput The {@link BuildOutput}
     */
    BuildOutputPanel(BuildOutput buildOutput)
    {
        super(new BorderLayout());
        
        this.buildOutput = buildOutput;
        
        this.uniqueCompilerWarnings = 
            listWithUniqueElements(buildOutput.getCompilerWarnings());
        
        this.uniqueCompilerErrors = 
            listWithUniqueElements(buildOutput.getCompilerErrors());
        
        this.uniqueLinkerWarnings = 
            listWithUniqueElements(buildOutput.getLinkerWarnings());
        
        this.uniqueLinkerErrors = 
            listWithUniqueElements(buildOutput.getLinkerErrors());
        
        this.uniqueListSelectionHandler = new UniqueListSelectionHandler();
        
        JPanel overviewPanel = createOverviewPanel();
        add(overviewPanel, BorderLayout.NORTH);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);
        
        JComponent tablesComponent = createTablesComponent();
        mainSplitPane.setTopComponent(tablesComponent);
        
        JPanel infoPanel = createInfoPanel();
        mainSplitPane.setBottomComponent(infoPanel);
        
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the panel that shows an overview of the {@link #buildOutput}
     * 
     * @return The overview panel
     */
    private JPanel createOverviewPanel()
    {
        JPanel overviewPanel = new JPanel(new GridBagLayout());
        int row = 0;
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Project "+buildOutput.getNumber()+":"),
            new JLabel(boldHtml(buildOutput.getProjectName())));
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Compiler warnings:"),
            new JLabel(boldHtml(buildOutput.getCompilerWarnings().size())));
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Compiler errors:"),
            new JLabel(boldHtml(buildOutput.getCompilerErrors().size())));
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Linker warnings:"),
            new JLabel(boldHtml(buildOutput.getLinkerWarnings().size())));
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Linker errors:"),
            new JLabel(boldHtml(buildOutput.getLinkerErrors().size())));
        
        JComponent outputFileNameComponent = null;
        String outputFileName = buildOutput.getOutputFileName();
        if (outputFileName == null)
        {
            outputFileNameComponent = new JLabel(boldHtml("(none)"));
        }
        else
        {
            outputFileNameComponent =
                Utils.createFileTextField(outputFileName);
            outputFileNameComponent.setFont(
                outputFileNameComponent.getFont().deriveFont(Font.BOLD));
        }
        GridBagLayouts.addRow(overviewPanel, row++, 1, 
            new JLabel("Output file:"),
            outputFileNameComponent);
        
        overviewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createTitledBorder("Overview")));
        return overviewPanel;
    }
    
    
    
    
    /**
     * Create the component that contains the tables for the (unique) compiler
     * warnings and errors, and the linker warnings and errors
     * 
     * @return The tables component
     */
    private JComponent createTablesComponent()
    {
        JTabbedPane tablesTabbedPane = new JTabbedPane();
        if (!uniqueCompilerWarnings.isEmpty())
        {
            JComponent compilerWarningsPanel = 
                createCompilerMessageTablePanel(
                    uniqueCompilerWarnings,
                    code -> LinkMappings.getCompilerWarningLink(code));
            String s = "(" + uniqueCompilerWarnings.size() + ")";
            tablesTabbedPane.addTab(
                "Unique compiler warnings "+s, compilerWarningsPanel);
        }
        if (!uniqueCompilerErrors.isEmpty())
        {
            JComponent compilerErrorsPanel = 
                createCompilerMessageTablePanel(
                    uniqueCompilerErrors, 
                    code -> LinkMappings.getCompilerErrorLink(code));
            String s = "(" + uniqueCompilerErrors.size() + ")";
            tablesTabbedPane.addTab(
                "Unique compiler errors "+s, compilerErrorsPanel);
        }
        if (!uniqueLinkerWarnings.isEmpty())
        {
            JComponent linkerWarningsPanel =
                createLinkerMessageTablePanel(
                    uniqueLinkerWarnings,
                    code -> LinkMappings.getLinkerWarningLink(code));
            String s = "(" + uniqueLinkerWarnings.size() + ")";
            tablesTabbedPane.addTab(
                "Unique linker warnings "+s, linkerWarningsPanel);
        }
        if (!uniqueLinkerErrors.isEmpty())
        {
            JComponent linkerErrorsPanel =
                createLinkerMessageTablePanel(
                    uniqueLinkerErrors,
                    code -> LinkMappings.getLinkerErrorLink(code));
            String s = "(" + uniqueLinkerErrors.size() + ")";
            tablesTabbedPane.addTab(
                "Unique linker errors "+s, linkerErrorsPanel);
        }
        return tablesTabbedPane;
    }


    
    /**
     * Create a panel containing a table with the given messages
     * 
     * @param compilerMessages The messages
     * @return The panel
     */
    private JComponent createCompilerMessageTablePanel(
        List<? extends CompilerMessage> compilerMessages,
        Function<Object, String> codeLinkLookup)
    {
        JPanel tablePanel = new JPanel(new GridLayout(1,1));
        
        DefaultTableModel tableModel = 
            Utils.createUneditableTableModel(
                "File", "Line", "Code", "Message" );
        for (CompilerMessage compilerMessage : compilerMessages)
        {
            Object rowData[] = new Object[] {
                compilerMessage.getNormalizedFileName(),
                compilerMessage.getLineNumber(), 
                compilerMessage.getCode(),
                compilerMessage.getMessage()
            };
            tableModel.addRow(rowData);
        }
        JTable table = new JTable(tableModel);

        uniqueListSelectionHandler.add(table.getSelectionModel());
        table.getSelectionModel().addListSelectionListener(
            uniqueListSelectionHandler);
        table.getSelectionModel().addListSelectionListener(
            new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                int viewRow = table.getSelectedRow();
                if (viewRow == -1)
                {
                    updateForSelectedCompilerMessage(null);
                }
                else
                {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    CompilerMessage compilerMessage = 
                        compilerMessages.get(modelRow);
                    updateForSelectedCompilerMessage(compilerMessage);
                }
            }
        });
        
        JTables.adjustColumnWidths(table, Short.MAX_VALUE);
        tablePanel.add(new JScrollPane(table));
        
        JPopupMenu popupMenu = new JPopupMenu();
        table.addMouseListener(new LocationBasedPopupHandler(popupMenu));

        int fileNameColumnIndex = 0;

        Action openFileAction =
            new OpenFileAction(fileNameColumnIndex);
        popupMenu.add(new JMenuItem(openFileAction));
        Action openContainingFolderAction =
            new OpenContainingFolderAction(fileNameColumnIndex);
        popupMenu.add(new JMenuItem(openContainingFolderAction));
        
        int codeColumnIndex = 2;
        Action browseToAction = 
            new BrowseToAction(codeColumnIndex, codeLinkLookup);
        popupMenu.add(new JMenuItem(browseToAction));
        
        return tablePanel;
    }
    

    /**
     * Create a panel containing a table with the given messages
     * 
     * @param linkerMessages The messages
     * @return The panel
     */
    private JComponent createLinkerMessageTablePanel(
        List<? extends LinkerMessage> linkerMessages,
        Function<Object, String> codeLinkLookup)
    {
        JPanel tablePanel = new JPanel(new GridLayout(1,1));
        
        DefaultTableModel tableModel = 
            Utils.createUneditableTableModel(
                "Code", "Message" );
        for (LinkerMessage linkerMessage : linkerMessages)
        {
            Object rowData[] = new Object[] {
                linkerMessage.getCode(),
                linkerMessage.getMessage()
            };
            tableModel.addRow(rowData);
        }
        JTable table = new JTable(tableModel);
        
        uniqueListSelectionHandler.add(table.getSelectionModel());
        table.getSelectionModel().addListSelectionListener(
            uniqueListSelectionHandler);
        table.getSelectionModel().addListSelectionListener(
            new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    updateForSelectedCompilerMessage(null);
                }
            });
        
        JTables.adjustColumnWidths(table, Short.MAX_VALUE);
        tablePanel.add(new JScrollPane(table));

        JPopupMenu popupMenu = new JPopupMenu();
        int codeColumnIndex = 0;
        Action browseToAction = 
            new BrowseToAction(codeColumnIndex, codeLinkLookup);
        table.addMouseListener(new LocationBasedPopupHandler(popupMenu));
        popupMenu.add(new JMenuItem(browseToAction));
        
        return tablePanel;
    }
    
    /**
     * This method will be called when a {@link CompilerMessage} was selected
     * in one of the tables (or when a {@link LinkerMessage} was selected - 
     * then, <code>null</code> is passed in as an argument)
     * 
     * @param compilerMessage The selected {@link CompilerMessage}. 
     * May be <code>null</code>.
     */
    private void updateForSelectedCompilerMessage(
        CompilerMessage compilerMessage)
    {
        compilerMessagePanel.setCompilerMessage(compilerMessage);   
        if (compilerMessage == null)
        {
            updateForwardTree(null);
            updateReverseTree(null);
        }
        else
        {
            updateForwardTree(compilerMessage.getNormalizedFileName());
            updateReverseTree(compilerMessage.getNormalizedFileName());
        }
    }
    
    
    /**
     * Create the panel at the bottom, containing tabs with additional
     * information about the build
     * 
     * @return The panel
     */
    private JPanel createInfoPanel()
    {
        JPanel infoPanel = new JPanel(new GridLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("General", createGeneralInfoPanel());
        tabbedPane.addTab("Input Files", createInputFileNamesPanel());
        tabbedPane.addTab("Output", createOutputInfoPanel());
        
        if (!buildOutput.getIncludes().isEmpty())
        {
            tabbedPane.addTab("Includes", createIncludesInfoPanel());
        }
        
        infoPanel.add(tabbedPane);
        return infoPanel;
    }
    
    /**
     * Create the panel showing general information (currently, this
     * is only information about the (selected) {@link CompilerMessage},
     * if any)
     * 
     * @return The panel
     */
    private JPanel createGeneralInfoPanel()
    {
        JPanel generalInfoPanel = new JPanel(new GridLayout(1,1));
        compilerMessagePanel = new CompilerMessagePanel();
        generalInfoPanel.add(compilerMessagePanel);
        return generalInfoPanel;
    }

    /**
     * Creates a panel that shows the ({@link BuildOutput#getInputFileNames()}
     * from the {@link #buildOutput} in a single text area.
     * 
     * @return The panel
     */
    private JPanel createInputFileNamesPanel()
    {
        JPanel inputInfoPanel = new JPanel(new BorderLayout());
        
        inputInfoPanel.add(new JLabel("Input file names:"), BorderLayout.NORTH);
        
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        StringBuilder sb = new StringBuilder();
        for (String inputFileName : buildOutput.getInputFileNames())
        {
            sb.append(inputFileName).append("\n");
        }
        outputTextArea.setText(sb.toString());
        
        inputInfoPanel.add(
            new JScrollPane(outputTextArea), BorderLayout.CENTER);
        return inputInfoPanel;
    }
    
    /**
     * Creates a panel that shows the output ({@link BuildOutput#getLines()}
     * from the {@link #buildOutput} in a single text area.
     * 
     * @return The panel
     */
    private JPanel createOutputInfoPanel()
    {
        JPanel outputInfoPanel = new JPanel(new BorderLayout());
        
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        StringBuilder sb = new StringBuilder();
        for (String line : buildOutput.getLines())
        {
            sb.append(line).append("\n");
        }
        outputTextArea.setText(sb.toString());
        
        outputInfoPanel.add(
            new JScrollPane(outputTextArea), BorderLayout.CENTER);
        return outputInfoPanel;
    }
    
    /**
     * Creates the panel that shows the include hierarchies, as created
     * when the compilation was run with "/showIncludes"
     * 
     * @return The panel
     */
    private JPanel createIncludesInfoPanel()
    {
        JPanel includesInfoPanel = new JPanel(new GridLayout(1,1));
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.3);
        
        TreeModel includesTreeModel = createIncludesTreeModel(buildOutput);
        JScrollPane includesTreeScrollPane = 
            new JScrollPane(new JTree(includesTreeModel));
        includesTreeScrollPane.setBorder(
            BorderFactory.createTitledBorder("All includes:"));
        mainSplitPane.setLeftComponent(includesTreeScrollPane);
        
        
        JSplitPane treesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treesSplitPane.setResizeWeight(0.5);
        
        JPanel forwardIncludesTreePanel = new JPanel(new BorderLayout());
        forwardIncludesTree = new JTree(new DefaultMutableTreeNode());
        JScrollPane forwardIncludesTreeScrollPane = 
            new JScrollPane(forwardIncludesTree);
        forwardIncludesTreeScrollPane.setBorder(
            BorderFactory.createTitledBorder("Forward includes:"));
        forwardIncludesTreePanel.add(
            forwardIncludesTreeScrollPane, BorderLayout.CENTER);

        JSplitPane reverseIncludesTreeSplitPane = 
            new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        reverseIncludesTreeSplitPane.setResizeWeight(0.75);
        
        reverseIncludesTree = new JTree(new DefaultMutableTreeNode());
        JScrollPane reverseIncludesTreeScrollPane = 
            new JScrollPane(reverseIncludesTree);
        reverseIncludesTreeScrollPane.setBorder(
            BorderFactory.createTitledBorder("Reverse includes:"));
        reverseIncludesTreeSplitPane.setTopComponent(
            reverseIncludesTreeScrollPane);

        reverseTreeLeavesTextArea = new JTextArea();
        reverseTreeLeavesTextArea.setEditable(false);
        JScrollPane leavesTextAreaScrollPane = 
            new JScrollPane(reverseTreeLeavesTextArea);
        leavesTextAreaScrollPane.setBorder(
            BorderFactory.createTitledBorder("Leaf nodes:"));
        reverseIncludesTreeSplitPane.setBottomComponent(
            leavesTextAreaScrollPane);
        
        treesSplitPane.setLeftComponent(forwardIncludesTreePanel);
        treesSplitPane.setRightComponent(reverseIncludesTreeSplitPane);
        mainSplitPane.setRightComponent(treesSplitPane);
        
        includesInfoPanel.add(mainSplitPane);
        return includesInfoPanel;
    }



    /**
     * Update the "forward" includes tree, showing the {@link Include}s 
     * that are rooted at the include with the given file name
     * 
     * @param normalizedFileName The file name
     */
    private void updateForwardTree(String normalizedFileName)
    {
        if (buildOutput.getIncludes().isEmpty())
        {
            return;
        }
        if (normalizedFileName == null || normalizedFileName.trim().isEmpty())
        {
            return;
        }
        TreeModel treeModel = createForwardTreeModel(normalizedFileName);
        forwardIncludesTree.setModel(treeModel);
        JTrees.expandAllFixedHeight(forwardIncludesTree);
    }
    
    /**
     * Create the "forward" includes tree model
     * 
     * @param normalizedFileName The file name
     */
    private TreeModel createForwardTreeModel(String normalizedFileName)
    {
        Map<String, Include> includes = buildOutput.getIncludes();
        Include include = includes.get(normalizedFileName);
        if (include == null)
        {
            return new DefaultTreeModel(new DefaultMutableTreeNode("(none)"));
        }
        DefaultMutableTreeNode root = 
            buildForwardTree(include, new LinkedHashSet<Include>());
        return new DefaultTreeModel(root);
    }
    
    /**
     * Recursively create the "forward" includes tree nodes
     * 
     * @param include The root include
     * @param processed The processed includes
     */
    private DefaultMutableTreeNode buildForwardTree(
        Include include, Set<Include> processed)
    {
        if (processed.contains(include))
        {
            return new DefaultMutableTreeNode("(cycle: "+include+")");
        }
        processed.add(include);
        DefaultMutableTreeNode node = 
            new DefaultMutableTreeNode(include);
        for (Include childInclude : include.getChildren())
        {
            DefaultMutableTreeNode childNode = 
                buildForwardTree(childInclude, processed);
            node.add(childNode);
        }
        return node;
    }
    

    /**
     * Update the "reverse" includes tree, showing the {@link Include}s 
     * that are rooted at the include with the given file name
     * 
     * @param normalizedFileName The file name
     */
    private void updateReverseTree(String normalizedFileName)
    {
        if (buildOutput.getIncludes().isEmpty())
        {
            return;
        }
        if (normalizedFileName == null || normalizedFileName.trim().isEmpty())
        {
            return;
        }
        TreeModel treeModel = createReverseTreeModel(normalizedFileName);
        reverseIncludesTree.setModel(treeModel);
        JTrees.expandAllFixedHeight(reverseIncludesTree);
        
        StringBuilder sb = new StringBuilder();
        Map<String, Include> includes = buildOutput.getIncludes();
        Include include = includes.get(normalizedFileName);
        if (include != null)
        {
            List<Include> leafIncludes = computeReverseLeafIncludes(include);
            for (Include leafInclude : leafIncludes)
            {
                sb.append(leafInclude.getNormalizedPath()).append("\n");
            }
        }
        reverseTreeLeavesTextArea.setText(sb.toString());
    }
    
    /**
     * Create the "reverse" includes tree model
     * 
     * @param normalizedFileName The file name
     */
    private TreeModel createReverseTreeModel(String normalizedFileName)
    {
        Map<String, Include> includes = buildOutput.getIncludes();
        Include include = includes.get(normalizedFileName);
        if (include == null)
        {
            return new DefaultTreeModel(new DefaultMutableTreeNode("(none)"));
        }
        DefaultMutableTreeNode root = 
            buildReverseTree(include, new LinkedHashSet<Include>());
        return new DefaultTreeModel(root);
    }
    
    /**
     * Recursively create the "reverse" includes tree nodes
     * 
     * @param include The root include
     * @param processed The processed includes
     */
    private DefaultMutableTreeNode buildReverseTree(
        Include include, Set<Include> processed)
    {
        if (processed.contains(include))
        {
            return new DefaultMutableTreeNode("(cycle: "+include+")");
        }
        processed.add(include);
        DefaultMutableTreeNode node = 
            new DefaultMutableTreeNode(include);
        for (Include parentInclude : include.getParents())
        {
            DefaultMutableTreeNode childNode = 
                buildReverseTree(parentInclude, processed);
            node.add(childNode);
        }
        return node;
    }

    
    /**
     * Compute the "leaf" nodes of the REVERSE tree rooted at the given
     * {@link Include}. This is the set of all files that include other
     * files, and eventually include the given {@link Include}
     * 
     * @param include The root {@link Include}
     * @return The leaf {@link Include}s
     */
    private List<Include> computeReverseLeafIncludes(Include include)
    {
        Set<Include> leafIncludes = new LinkedHashSet<Include>();
        computeReverseLeafIncludes(include, leafIncludes);
        return new ArrayList<Include>(leafIncludes);
    }
    
    /**
     * Recursively compute the "leaf" nodes of the REVERSE tree rooted at the 
     * given {@link Include}. This is the set of all files that include other
     * files, and eventually include the given {@link Include}
     * 
     * @param include The root {@link Include}
     * @param leafIncludes The leaf {@link Include}s
     */
    private static void computeReverseLeafIncludes(
        Include include, Set<Include> leafIncludes)
    {
        if (include.getParents().isEmpty())
        {
            leafIncludes.add(include);
        }
        else
        {
            for (Include parent : include.getParents())
            {
                computeReverseLeafIncludes(parent, leafIncludes);
            }
        }
    }
    
    
    /**
     * Create a tree model containing all {@link Include} information that is
     * contained in the given {@link BuildOutput}
     * 
     * @param buildOutput The {@link BuildOutput}
     * @return The tree model
     */
    private static TreeModel createIncludesTreeModel(BuildOutput buildOutput)
    {
        DefaultMutableTreeNode root = 
            new DefaultMutableTreeNode("All Includes");
        Map<String, Include> includes = buildOutput.getIncludes();
        
        for (Entry<String, Include> entry : includes.entrySet())
        {
            Include include = entry.getValue();
            DefaultMutableTreeNode includeRoot = 
                buildIncludesTree(include, new HashSet<Include>());
            root.add(includeRoot);
        }
        return new DefaultTreeModel(root);
    }
    
    /**
     * Recursively build the tree nodes for the hierarchy rooted at the
     * given {@link Include}
     * 
     * @param include The {@link Include}
     * @param processed The {@link Include}s that already have been processed,
     * to avoid cycles
     * @return The tree node
     */
    private static DefaultMutableTreeNode buildIncludesTree(
        Include include, Set<Include> processed)
    {
        if (processed.contains(include))
        {
            return new DefaultMutableTreeNode("(cycle: "+include+")");
        }
        DefaultMutableTreeNode node = 
            new DefaultMutableTreeNode(include);
        processed.add(include);
        for (Include childInclude : include.getChildren())
        {
            //System.out.println("Build for "+childInclude);
            DefaultMutableTreeNode childNode = 
                buildIncludesTree(childInclude, processed);
            node.add(childNode);
        }
        return node;
    }
    
    /**
     * Creates a string representation of the given object, in HTML tags
     * that cause a bold font
     * 
     * @param object The object
     * @return The string
     */
    private static String boldHtml(Object object)
    {
        return "<html><b>"+object+"</b></html>";
    }
 
    /**
     * Returns a new list containing the unique elements from the given
     * collection
     * 
     * @param collection The input collection
     * @return The list
     */
    private static <T> List<T> listWithUniqueElements(
        Collection<? extends T> collection)
    {
        return new ArrayList<T>(new LinkedHashSet<T>(collection));
    }
    
    
}