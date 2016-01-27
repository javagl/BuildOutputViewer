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
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.javagl.swing.tasks.SwingTask;
import de.javagl.swing.tasks.SwingTaskExecutors;

/**
 * The main class of the build output viewer
 */
public class BuildOutputViewer
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BuildOutputViewer.class.getName());
    
    /**
     * The entry point of the application
     * 
     * @param args Not used
     */
    public static void main(String[] args)
    {
        initLogging();
        setSystemLookAndFeel();
        SwingUtilities.invokeLater(
            () -> new BuildOutputViewer());
    }
    
    /**
     * Initialize the logging using the logging.properties resource
     */
    private static void initLogging()
    {
        try (InputStream inputStream =
            BuildOutputViewer.class.getResourceAsStream(
                "/logging.properties"))
        {
            if (inputStream != null)
            {
                LogManager.getLogManager().readConfiguration(inputStream);
            }
        }
        catch (IOException e)
        {
            logger.warning(e.getMessage());
        }
    }
    
    
    /**
     * Try to set the default system look and feel, ignoring all exceptions
     */
    private static void setSystemLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e)
        {
            // Ignore
        }
        catch (ClassNotFoundException e)
        {
            // Ignore
        }
        catch (InstantiationException e)
        {
            // Ignore
        }
        catch (IllegalAccessException e)
        {
            // Ignore
        }
    }
    
    
    /**
     * The Action for opening a build log file
     */
    private final Action openFileAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -5125243029591873126L;

        // Initialization
        {
            putValue(NAME, "Open build log file...");
            putValue(SHORT_DESCRIPTION, "Open a build log file");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            openFile();
        }
    };
    
    /**
     * The Action for requesting the user to input (copy and paste) a build log
     */
    private final Action inputBuildLogAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -512524302959873126L;

        // Initialization
        {
            putValue(NAME, "Input build log...");
            putValue(SHORT_DESCRIPTION, "Input a build log into a text area");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            inputBuildLog();
        }
    };

    /**
     * The Action for saving an analyzed build log in a directory
     */
    private final Action saveAsAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = 2304472601624430742L;

        // Initialization
        {
            putValue(NAME, "Save as...");
            putValue(SHORT_DESCRIPTION, "Save the current build output");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            saveAs();
        }
    };
    
    /**
     * The Action to exit the application 
     */
    private final Action exitAction = new AbstractAction()
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -8426096621732848545L;

        // Initialization
        {
            putValue(NAME, "Exit");
            putValue(SHORT_DESCRIPTION, "Exit the application");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            exit();
        }
    };
    
    
    /**
     * The main application frame
     */
    private final JFrame frame;
    
    /**
     * The FileChooser for opening files
     */
    private final JFileChooser openFileChooser;
    
    /**
     * The FileChooser for saving the build output to a directory
     */
    private final JFileChooser saveToDirectoryChooser;

    /**
     * The {@link BuildOutputViewerMainPanel}
     */
    private final BuildOutputViewerMainPanel buildOutputViewerMainPanel;
    
    /**
     * The {@link BuildOutputProcessor} that receives the build
     * output data and dispatches it to {@link BuildOutput} instances 
     */
    private final BuildOutputProcessor buildOutputProcessor;
    
    /**
     * Whether the current build output is not saved yet
     */
    private boolean currentStateIsUnsaved;
    
    /**
     * Default constructor. May only be called on the Event Dispatch Thread.
     */
    BuildOutputViewer()
    {
        this.frame = new JFrame("BuildOutputViewer");
        this.openFileChooser = createOpenFileChooser();
        this.saveToDirectoryChooser = createSaveToDirectoryChooser();
        
        this.buildOutputProcessor = new BuildOutputProcessor();
        this.currentStateIsUnsaved = false;
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });
        
        JMenuBar menuBar = new JMenuBar();
        JMenu analyzeMenu = createAnalyzeMenu();
        menuBar.add(analyzeMenu);
        frame.setJMenuBar(menuBar);
        
        frame.getContentPane().setLayout(new BorderLayout());
        buildOutputViewerMainPanel = new BuildOutputViewerMainPanel();
        frame.getContentPane().add(
            buildOutputViewerMainPanel, BorderLayout.CENTER);
        
        DisplayMode displayMode = 
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDisplayMode();
        
        frame.setSize(displayMode.getWidth(), displayMode.getHeight() - 380);
        frame.setLocation(0, 0);
        frame.setVisible(true);
    }
    
    /**
     * Processes the build log from the given stream in a background thread,
     * passes the resulting {@link BuildOutput} objects, to the
     * {@link BuildOutputViewerMainPanel}, and closes the stream
     * 
     * @param inputStream The input stream
     */
    private void processBuildLogInputStream(InputStream inputStream)
    {
        BufferedReader bufferedReader = 
            new BufferedReader(new InputStreamReader(
                inputStream, StandardCharsets.UTF_8));
        
        SwingTask<Void, Void> swingTask = new SwingTask<Void, Void>()
        {
            private boolean finished = false;
            
            @Override
            protected Void doInBackground() throws Exception
            {
                setProgress(-1.0);
                setMessage("Reading...");
                List<String> lines = new ArrayList<String>();
                String line = null;
                while (true)
                {
                    line = bufferedReader.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    lines.add(line);
                }
                buildOutputProcessor.clear();
                finished = buildOutputProcessor.process(lines, processedLineCount ->
                {
                    if (processedLineCount % 1000 == 0)
                    {
                        double progress = 
                            (double)processedLineCount / lines.size();
                        setProgress(progress);
                        setMessage("Processing line " + processedLineCount + 
                            " of " + lines.size());
                    }
                });
                return null;
            }
            
            @Override
            protected void done()
            {
                if (finished)
                {
                    Collection<BuildOutput> buildOutputs = 
                        buildOutputProcessor.getBuildOutputs();
                    buildOutputViewerMainPanel.setBuildOutputs(buildOutputs);
                }
                else
                {
                    buildOutputViewerMainPanel.setBuildOutputs(null);
                }
            }
        };
        SwingTaskExecutors
            .create(swingTask)
            .setTitle("Reading...")
            .setCancelable(true)
            .setDialogUncaughtExceptionHandler()
            .build()
            .execute();
        
        currentStateIsUnsaved = true;
        
        try
        {
            inputStream.close();
        }
        catch (IOException e)
        {
            logger.warning(e.getMessage());
        }
    }


    /**
     * Create and return a file chooser for opening a build log file
     *  
     * @return The file chooser
     */
    private JFileChooser createOpenFileChooser()
    {
        JFileChooser openFileChooser = new JFileChooser(".");
        //openFileChooser.setFileFilter(
        //    new FileNameExtensionFilter("Build Log Files", "log"));
        return openFileChooser;
    }
    
    /**
     * Create and return a file chooser for saving the analyzed build
     * output to a directory
     *  
     * @return The file chooser
     */
    private JFileChooser createSaveToDirectoryChooser()
    {
        JFileChooser saveToDirectoryChooser = new JFileChooser(".");
        saveToDirectoryChooser.setFileSelectionMode(
            JFileChooser.DIRECTORIES_ONLY);
        return saveToDirectoryChooser;
    }
    
    /**
     * Create the 'Analyze' menu
     * 
     * @return The menu
     */
    private JMenu createAnalyzeMenu()
    {
        JMenu fileMenu = new JMenu("Analyze");
        fileMenu.setMnemonic(KeyEvent.VK_A);

        fileMenu.add(new JMenuItem(openFileAction));
        fileMenu.add(new JMenuItem(inputBuildLogAction));
        fileMenu.add(new JMenuItem(saveAsAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(exitAction));
        
        return fileMenu;
    }
    
    /**
     * Shows a file chooser for selecting a file that will be passed
     * to {@link #openFile(File)}.
     */
    private void openFile()
    {
        if (currentStateIsUnsaved)
        {
            int confirmState = 
                JOptionPane.showConfirmDialog(frame, 
                    "Current build output was not saved, do you want " +
                    "to save before opening a new file?", 
                    "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmState == JOptionPane.YES_OPTION)
            {
                boolean saved = saveAs();
                if (!saved)
                {
                    return;
                }
            }
            else if (confirmState == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        int returnState = openFileChooser.showOpenDialog(frame);
        if (returnState == JFileChooser.APPROVE_OPTION) 
        {
            File file = openFileChooser.getSelectedFile();
            saveToDirectoryChooser.setSelectedFile(file.getParentFile());
            openFile(file);
        }        
    }
    
    /**
     * Read a {@link FlowWorkspace} from the given file, and 
     * pass it to {@link #initEditor(MutableFlowWorkspace)}.
     * 
     * @param file The file to open
     */
    private void openFile(File file)
    {
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
            processBuildLogInputStream(inputStream);
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(frame,
                "File not found: "+file,
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } 
    }
    
    /**
     * Show a dialog prompting the user to input the build log 
     */
    private void inputBuildLog()
    {
        if (currentStateIsUnsaved)
        {
            int confirmState = 
                JOptionPane.showConfirmDialog(frame, 
                    "Current build output was not saved, do you want " +
                    "to save before entering a new build log?", 
                    "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmState == JOptionPane.YES_OPTION)
            {
                boolean saved = saveAs();
                if (!saved)
                {
                    return;
                }
            }
            else if (confirmState == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        BuildLogInputDialog buildLogInputDialog = 
            new BuildLogInputDialog(frame);
        buildLogInputDialog.setVisible(true);
        
        int option = buildLogInputDialog.getOption();
        if (option == JOptionPane.OK_OPTION)
        {
            String buildLogText = buildLogInputDialog.getText();
            InputStream inputStream = 
                new ByteArrayInputStream(buildLogText.getBytes());
            processBuildLogInputStream(inputStream);
        }        
    }
    
    
    
    /**
     * Shows a file chooser for selecting a directory that will be passed
     * to {@link #saveToDirectory(File)}
     * 
     * @return Whether the save operation completed without errors
     */
    private boolean saveAs()
    {
        int returnState = saveToDirectoryChooser.showSaveDialog(frame);
        if (returnState == JFileChooser.APPROVE_OPTION) 
        {
            File file = saveToDirectoryChooser.getSelectedFile();
            boolean saved = saveToDirectory(file);
            currentStateIsUnsaved = !saved;
            return saved;
        }       
        return false;
    }

    
    /**
     * Save the current build output to the given directory
     * 
     * @param directory The directory
     * @return Whether the save operation completed without errors
     */
    private boolean saveToDirectory(File directory)
    {
        try
        {
            List<BuildOutput> buildOutputs = 
                buildOutputViewerMainPanel.getBuildOutputs();
            saveBuildOutputs(buildOutputs, directory);
            return true;
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(frame,
                "Error while saving to "+directory,
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    
    /**
     * Write all lines that are contained in the given {@link BuildOutput}s
     * into files that are named according to the project name of the
     * respective {@link BuildOutput}, in the given directory
     * 
     * @param buildOutputs The {@link BuildOutput}s
     * @param directory The target directory
     * @throws IOException If an IO error occurs
     */
    private void saveBuildOutputs(
        List<BuildOutput> buildOutputs, File directory) throws IOException
    {
        if (!directory.exists())
        {
            if (!directory.mkdirs())
            {
                throw new IOException("Could not create directory "+directory);
            }
        }
        for (BuildOutput buildOutput : buildOutputs)
        {
            File file = new File(
                directory, buildOutput.getProjectName()+".txt");
            try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file))))
            {
                for (String line : buildOutput.getLines())
                {
                    pw.println(line);
                }
                pw.close();
            }
        }
    }

    /**
     * Exits the application by disposing the main frame. 
     * If there are unsaved changes, the user is asked 
     * for confirmation. 
     */
    private void exit()
    {
        if (currentStateIsUnsaved)
        {
            int confirmState = 
                JOptionPane.showConfirmDialog(frame, 
                    "The current build output was not saved. " + 
                    "Do you want to save before exiting?", 
                    "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmState == JOptionPane.YES_OPTION)
            {
                boolean saved = saveAs();
                if (!saved)
                {
                    return;
                }
            }
            else if (confirmState == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        frame.setVisible(false);
        frame.dispose();
    }
    

}


