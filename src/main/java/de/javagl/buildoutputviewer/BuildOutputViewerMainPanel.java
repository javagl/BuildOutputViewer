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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.javagl.common.ui.JSplitPanes;
import de.javagl.common.ui.JTables;

/**
 * The main panel of the build output viewer, consisting of the table
 * with the project list, and the desktop pane that shows the individual
 * build outputs
 */
class BuildOutputViewerMainPanel extends JPanel
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 6167226106222962776L;
    
    /**
     * The index of the column in the project list table that 
     * indicates whether the build was skipped
     */
    private static final int SKIPPED_COLUMN_INDEX = 1;
    
    /**
     * The index of the column in the project list table that 
     * contains the number of compiler warnings
     */
    private static final int COMPILER_WARNINGS_COLUMN_INDEX = 2;

    /**
     * The index of the column in the project list table that 
     * contains the number of compiler errors
     */
    private static final int COMPILER_ERRORS_COLUMN_INDEX = 3;
    
    /**
     * The index of the column in the project list table that 
     * contains the number of linker warnings
     */
    private static final int LINKER_WARNINGS_COLUMN_INDEX = 4;

    /**
     * The index of the column in the project list table that 
     * contains the number of linker errors
     */
    private static final int LINKER_ERRORS_COLUMN_INDEX = 5;

    /**
     * The index of the column in the project list table that 
     * indicates whether the project has include information
     */
    private static final int INCLUDE_COLUMN_INDEX = 4;

    /**
     * The model for the table showing the projects overview
     */
    private DefaultTableModel projectsTableModel;
    
    /**
     * The table showing the projects overview
     */
    private JTable projectsTable;
    
    /**
     * The row sorter for the projects table
     */
    private TableRowSorter<DefaultTableModel> projectsTableRowSorter;
    
    /**
     * The desktop pane that contains the internal frames for the
     * individual {@link BuildOutput}s in {@link BuildOutputPanel}s
     */
    private JDesktopPane desktopPane;
    
    /**
     * The {@link BuildOutput}s that are shown in this panel
     */
    private List<BuildOutput> buildOutputs = Collections.emptyList();
    
    /**
     * The map from {@link BuildOutput}s to the frames that they are
     * currently shown in
     */
    private final Map<BuildOutput, JInternalFrame> openedBuildOutputFrames;
    
    /**
     * The Action for hiding the skipped builds from the table
     */
    private final Action hideSkippedBuildsAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -5125243029591873126L;

        // Initialization
        {
            putValue(NAME, "Hide skipped builds");
            putValue(SHORT_DESCRIPTION, 
                "Show skipped builds in the projects table");
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateProjectsTableFilter();
        }

    };

    /**
     * The Action for hiding builds from the table that have neither warnings
     * nor errors
     */
    private final Action hideBuildsWithoutMessagesAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -512524370591873126L;

        // Initialization
        {
            putValue(NAME, "Hide builds without messages");
            putValue(SHORT_DESCRIPTION, 
                "Hide builds that did generate warnings or errors");
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_W));
            putValue(SELECTED_KEY, true);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateProjectsTableFilter();
        }
    };

    /**
     * The Action for hiding builds from the table that have no include 
     * information
     */
    private final Action hideBuildsWithoutIncludesAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -512520295913873126L;

        // Initialization
        {
            putValue(NAME, "Hide builds without include information");
            putValue(SHORT_DESCRIPTION, 
                "Hide builds that did not generate include information " + 
                "because they did not have the /showIncludes flag set");
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateProjectsTableFilter();
        }
    };
    
    /**
     * Default constructor
     */
    BuildOutputViewerMainPanel()
    {
        super(new BorderLayout());

        this.openedBuildOutputFrames = 
            new LinkedHashMap<BuildOutput, JInternalFrame>();
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPanes.setDividerLocation(mainSplitPane, 0.2);
        
        JPanel overviewPanel = new JPanel(new BorderLayout());
        JPanel overviewControlPanel = new JPanel(new GridLayout(0,1));
        overviewControlPanel.add(
            new JCheckBox(hideSkippedBuildsAction));
        overviewControlPanel.add(
            new JCheckBox(hideBuildsWithoutMessagesAction));
        overviewControlPanel.add(
            new JCheckBox(hideBuildsWithoutIncludesAction));
        overviewPanel.add(overviewControlPanel, BorderLayout.NORTH);
        createProjectsTable();
        overviewPanel.add(new JScrollPane(projectsTable), BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(overviewPanel);
        
        desktopPane = new JDesktopPane();
        mainSplitPane.setRightComponent(desktopPane);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        updateProjectsTableFilter();
    }
    
    
    /**
     * Create the {@link #projectsTableModel}, {@link #projectsTable} and
     * {@link #projectsTableRowSorter}
     */
    private void createProjectsTable()
    {
        projectsTableModel = Utils.createUneditableTableModel(
            "Project", 
            "Skipped?", 
            "<html>Comp<br>Warn</html>", 
            "<html>Comp<br>Err</html>", 
            "<html>Link<br>Warn</html>", 
            "<html>Link<br>Err</html>", 
            "Includes?");        
        projectsTable = new JTable(projectsTableModel);
        projectsTable.getTableHeader().setReorderingAllowed(false);

        projectsTableRowSorter = 
            new TableRowSorter<DefaultTableModel>(projectsTableModel);
        projectsTable.setRowSorter(projectsTableRowSorter);
        
        projectsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int viewRow = projectsTable.rowAtPoint(e.getPoint());
                    int modelRow = 
                        projectsTable.convertRowIndexToModel(viewRow);
                    openBuildOutputFrame(buildOutputs.get(modelRow));
                }
            }
        });
        
        projectsTable.setToolTipText("Double-click to open");
    }

    /**
     * Open the internal frame for the given {@link BuildOutput} (or bring it
     * to the front, if it is already open)
     * 
     * @param buildOutput The {@link BuildOutput}
     */
    private void openBuildOutputFrame(BuildOutput buildOutput)
    {
        JInternalFrame buildOutputFrame = 
            openedBuildOutputFrames.get(buildOutput);
        if (buildOutputFrame != null)
        {
            buildOutputFrame.toFront();
            return;
        }
        buildOutputFrame = 
            new JInternalFrame(buildOutput.getProjectName(), 
                true, true, true, true);
        openedBuildOutputFrames.put(buildOutput, buildOutputFrame);
        buildOutputFrame.addInternalFrameListener(new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                openedBuildOutputFrames.remove(buildOutput);
            }
        });
        buildOutputFrame.getContentPane().add(
            new BuildOutputPanel(buildOutput));
        desktopPane.add(buildOutputFrame);
        buildOutputFrame.setLocation(0, 0);
        buildOutputFrame.setSize(desktopPane.getSize());
        buildOutputFrame.setVisible(true);
    }
    
    /**
     * Returns whether the given action is selected
     * 
     * @param action The action
     * @return Whether the given action is selected
     */
    private boolean isSelected(Action action)
    {
        Object value = action.getValue(AbstractAction.SELECTED_KEY);
        if (value instanceof Boolean)
        {
            return (Boolean)value;
        }
        return false;
    }
    
    /**
     * Update the filter for the {@link #projectsTable}, depending on the
     * state of the check boxes that are associated with the
     * {@link #hideSkippedBuildsAction}, 
     * {@link #hideBuildsWithoutMessagesAction} and
     * {@link #hideBuildsWithoutIncludesAction}
     */
    private void updateProjectsTableFilter()
    {
        RowFilter<TableModel, Object> isSkipped = 
            booleanValueFilter(SKIPPED_COLUMN_INDEX);
        
        RowFilter<TableModel, Object> hasCompilerWarnings = 
            RowFilter.numberFilter(
                ComparisonType.AFTER, 0, COMPILER_WARNINGS_COLUMN_INDEX);
        
        RowFilter<TableModel, Object> hasCompilerErrors = 
            RowFilter.numberFilter(
                ComparisonType.AFTER, 0, COMPILER_ERRORS_COLUMN_INDEX);

        RowFilter<TableModel, Object> hasLinkerWarnings = 
            RowFilter.numberFilter(
                ComparisonType.AFTER, 0, LINKER_WARNINGS_COLUMN_INDEX);
        
        RowFilter<TableModel, Object> hasLinkerErrors = 
            RowFilter.numberFilter(
                ComparisonType.AFTER, 0, LINKER_ERRORS_COLUMN_INDEX);
        
        RowFilter<TableModel, Object> hasMessages =
            RowFilter.orFilter(Arrays.asList(
                hasCompilerWarnings, hasCompilerErrors,
                hasLinkerWarnings, hasLinkerErrors));
        
        RowFilter<TableModel, Object> hasIncludes = 
            booleanValueFilter(INCLUDE_COLUMN_INDEX);
        
        RowFilter<TableModel, Object> r = trueFilter(); 
        if (isSelected(hideSkippedBuildsAction))
        {
            r = RowFilter.andFilter(Arrays.asList(r, 
                RowFilter.notFilter(isSkipped)));
        }
        if (isSelected(hideBuildsWithoutMessagesAction))
        {
            r = RowFilter.andFilter(Arrays.asList(r, hasMessages));
        }
        if (isSelected(hideBuildsWithoutIncludesAction))
        {
            r = RowFilter.andFilter(Arrays.asList(r, hasIncludes));
        }
        projectsTableRowSorter.setRowFilter(r);
        JTables.adjustColumnWidths(projectsTable, Short.MAX_VALUE);
    }
    
    /**
     * Set the {@link BuildOutput}s that should be shown in this panel.
     * A copy of the given collection will be stored internally. If
     * the collection is <code>null</code>, then an empty list will
     * be stored.
     * 
     * @param buildOutputs The {@link BuildOutput}s
     */
    void setBuildOutputs(Collection<? extends BuildOutput> buildOutputs)
    {
        if (buildOutputs == null)
        {
            this.buildOutputs = Collections.emptyList();
        }
        else
        {
            this.buildOutputs = new ArrayList<BuildOutput>(buildOutputs);
        }
        
        projectsTableModel.setRowCount(0);
        for (BuildOutput buildOutput : buildOutputs)
        {
            String projectName = buildOutput.getProjectName();
            boolean skipped = buildOutput.isSkippedBuild();
            List<CompilerMessage> compilerWarnings = 
                buildOutput.getCompilerWarnings();
            int numCompilerWarnings = compilerWarnings.size();
            List<CompilerMessage> compilerErrors = 
                buildOutput.getCompilerErrors();
            int numCompilerErrors = compilerErrors.size();
            List<LinkerMessage> linkerWarnings = 
                buildOutput.getLinkerWarnings();
            int numLinkerWarnings = linkerWarnings.size();
            List<LinkerMessage> linkerErrors = 
                buildOutput.getLinkerErrors();
            int numLinkerErrors = linkerErrors.size();
            boolean hasIncludes = !buildOutput.getIncludes().isEmpty();
            
            projectsTableModel.addRow(new Object[] 
            {
                projectName, skipped, numCompilerWarnings, numCompilerErrors, 
                numLinkerWarnings, numLinkerErrors, hasIncludes
            });
        }
        JTables.adjustColumnWidths(projectsTable, Short.MAX_VALUE);
    }
    
    /**
     * Returns an unmodifiable list containing the {@link BuildOutput}s
     * that have previously been set with {@link #setBuildOutputs(Collection)}
     * 
     * @return The {@link BuildOutput}s
     */
    List<BuildOutput> getBuildOutputs()
    {
        return Collections.unmodifiableList(buildOutputs);
    }
    
    
    /**
     * Utility method that returns a RowFilter that returns the value
     * of a Boolean in the specified table column
     * 
     * @param column The column
     * @return The filter 
     */
    private static <M> RowFilter<M, Object> booleanValueFilter(int column)
    {
        RowFilter<M, Object> rowFilter = new RowFilter<M, Object>() 
        {
            @Override
            public boolean include(Entry<? extends M, ?> entry)
            {
                Object value = entry.getValue(column);
                if (value instanceof Boolean)
                {
                    return (Boolean)value;
                }
                return false;
            }
        };
        return rowFilter;
    }
    
    /**
     * Returns a RowFilter that accepts all entries
     * 
     * @return The filter
     */
    private static <M> RowFilter<M, Object> trueFilter()
    {
        RowFilter<M, Object> rowFilter = new RowFilter<M, Object>() 
        {
            @Override
            public boolean include(Entry<? extends M, ?> entry)
            {
                return true;
            }
        };
        return rowFilter;
    }
    
}
