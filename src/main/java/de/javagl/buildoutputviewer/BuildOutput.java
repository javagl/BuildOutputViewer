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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * A class summarizing the build output of <i>one</i> project. The lines
 * of the build output that are passed to the {@link #processLinePayload}
 * method are associated with this build output via their "123>" prefix
 * that indicates the {@link #getNumber() build number}. 
 */
class BuildOutput
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BuildOutput.class.getName());
    
    /**
     * The number of this build, indicated by the "123>" pefix of 
     * the lines in the build output
     */
    private final int number;

    /**
     * The name of the project that this build output belongs to.
     * This is extracted from a line whose payload starts with
     * the {@link #BUILD_STARTED_NOTE_PREFIX} the 
     * {@link #SKIPPED_BUILD_NOTE_PREFIX} 
     */
    private String projectName;

    /**
     * Whether this build was skipped, as indicated by the
     * {@link #SKIPPED_BUILD_NOTE_PREFIX} of a line payload
     */
    private boolean skippedBuild;
    
    /**
     * All lines of the build output for this build, in their original form
     */
    private final List<String> lines;
    
    /**
     * A map from (normalized) file names to the {@link Include} objects
     * that form the include hierarchy that is printed when 
     * the "/showIncludes" parameter was given during the build
     */
    private final Map<String, Include> includes;
    
    /**
     * The list of {@link CompilerMessage}s that indicate compiler warnings
     */
    private final List<CompilerMessage> compilerWarnings;

    /**
     * The list of {@link CompilerMessage}s that indicate compiler errors
     */
    private final List<CompilerMessage> compilerErrors;
    
    /**
     * The list of {@link LinkerMessage}s that indicate warnings
     */
    private final List<LinkerMessage> linkerWarnings;

    /**
     * The list of {@link LinkerMessage}s that indicate errors
     */
    private final List<LinkerMessage> linkerErrors;
    
    /**
     * The normalized name of the file that was generated with this build
     */
    private String normalizedOutputFileName;
    
    /**
     * The list of input file names, as extracted from the build output
     * (this may not be completely accurate in all cases)
     */
    private final List<String> inputFileNames;
    

    /**
     * Creates a new build output for the build with the given number,
     * which was extracted from the "123>" prefix of a build log line
     * 
     * @param number The build number
     */
    BuildOutput(int number)
    {
        this.number = number;
        this.lines = new ArrayList<String>();
        this.includes = 
            new TreeMap<String, Include>(String.CASE_INSENSITIVE_ORDER);
        this.compilerWarnings = new ArrayList<CompilerMessage>();
        this.compilerErrors = new ArrayList<CompilerMessage>();
        this.linkerWarnings = new ArrayList<LinkerMessage>();
        this.linkerErrors = new ArrayList<LinkerMessage>();
        this.inputFileNames = new ArrayList<String>();
    }
    
    /**
     * Returns the build number, which was extracted from the "123>" prefix
     * of the build log line
     * 
     * @return The build number
     */
    int getNumber()
    {
        return number;
    }
    
    /**
     * Set the name of the project that this build output belongs to
     * 
     * @param projectName The project name
     */
    void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }
    
    /**
     * Returns the name of the project that this build output belongs to
     * 
     * @return The project name
     */
    String getProjectName()
    {
        return projectName;
    }
    
    
    /**
     * Set whether the build of the project was skipped
     * 
     * @param skippedBuild Whether the build was skipped
     */
    void setSkippedBuild(boolean skippedBuild)
    {
        this.skippedBuild = skippedBuild;
    }
    
    /**
     * Returns whether the build for this project was skipped
     * 
     * @return Whether the build was skipped
     */
    boolean isSkippedBuild()
    {
        return skippedBuild;
    }
    
    /**
     * Add the given line (as it appeared in the build output)
     * to this build output
     * 
     * @param line The line
     */
    void addLine(String line)
    {
        lines.add(line);
    }
    
    /**
     * Returns an unmodifiable list of the lines that belong to the
     * log that this build output was created from
     * 
     * @return The lines
     */
    List<String> getLines()
    {
        return Collections.unmodifiableList(lines);
    }
    
    /**
     * Returns the {@link Include} information for the given normalized
     * include path. If the corresponding {@link Include} does not yet
     * exist, it is created, stored internally, and returned.
     * 
     * @param normalizedIncludePath The normalized include path
     * @return The {@link Include} for the given path
     */
    Include getInclude(String normalizedIncludePath)
    {
        Include include = includes.get(normalizedIncludePath);
        if (include == null)
        {
            include = new Include(normalizedIncludePath);
            includes.put(normalizedIncludePath, include);
        }
        return include;
    }
    
    /**
     * Returns an unmodifiable map that maps normalized file names
     * to the {@link Include} objects that summarize the includes
     * of the respective file.<br>
     * <br>
     * Note that depending on which parts of the build have been run
     * with the "/showIncludes" parameter, this map may be empty or
     * not contain values for all input file names! 
     * 
     * @return The mapping from normalized file names to their {@link Include}s
     */
    Map<String, Include> getIncludes()
    {
        return Collections.unmodifiableMap(includes);
    }

    /**
     * Add the given {@link CompilerMessage} as a warning in this build output
     * 
     * @param compilerMessage The {@link CompilerMessage}
     */
    void addCompilerWarning(CompilerMessage compilerMessage)
    {
        compilerWarnings.add(compilerMessage);
    }
    
    /**
     * Returns an unmodifiable list containing the {@link CompilerMessage}s
     * that indicate compiler warnings in the build log
     * 
     * @return The {@link CompilerMessage}s that indicate warnings
     */
    List<CompilerMessage> getCompilerWarnings()
    {
        return Collections.unmodifiableList(compilerWarnings);
    }

    /**
     * Add the given {@link CompilerMessage} as an error in this build output
     * 
     * @param compilerMessage The {@link CompilerMessage}
     */
    void addCompilerError(CompilerMessage compilerMessage)
    {
        compilerErrors.add(compilerMessage);
    }
    
    /**
     * Returns an unmodifiable list containing the {@link CompilerMessage}s
     * that indicate compiler errors in the build log
     * 
     * @return The {@link CompilerMessage}s that indicate errors
     */
    List<CompilerMessage> getCompilerErrors()
    {
        return Collections.unmodifiableList(compilerErrors);
    }


    /**
     * Add the given {@link LinkerMessage} as a warning in this build output
     * 
     * @param linkerMessage The {@link LinkerMessage}
     */
    void addLinkerWarning(LinkerMessage linkerMessage)
    {
        linkerWarnings.add(linkerMessage);
    }

    /**
     * Returns an unmodifiable list containing the {@link CompilerMessage}s
     * that indicate linker warnings in the build log
     * 
     * @return The {@link LinkerMessage}s that indicate warnings
     */
    List<LinkerMessage> getLinkerWarnings()
    {
        return Collections.unmodifiableList(linkerWarnings);
    }

    /**
     * Add the given {@link LinkerMessage} as an error in this build output
     * 
     * @param linkerMessage The {@link LinkerMessage}
     */
    void addLinkerError(LinkerMessage linkerMessage)
    {
        linkerErrors.add(linkerMessage);
    }
    
    /**
     * Returns an unmodifiable list containing the {@link CompilerMessage}s
     * that indicate linker errors in the build log
     * 
     * @return The {@link LinkerMessage}s that indicate errors
     */
    List<LinkerMessage> getLinkerErrors()
    {
        return Collections.unmodifiableList(linkerErrors);
    }
    
    /**
     * Set the normalized name of the file that was generated with this build
     * 
     * @param normalizedOutputFileName The output file name
     */
    void setOutputFileName(String outputFileName)
    {
        this.normalizedOutputFileName = outputFileName;
    }

    /**
     * Returns the normalized name of the file that was generated with
     * this build
     * 
     * @return The output file name
     */
    String getOutputFileName()
    {
        return normalizedOutputFileName;
    }

    /**
     * Add the given string as one of the input file names of this build
     * 
     * @param inputFileName The input file name
     */
    void addInputFileName(String inputFileName)
    {
        inputFileNames.add(inputFileName);
    }
    
    /**
     * Returns an unmodifiable list containing the input file names of 
     * this build. Note that this list <i>might</i> also contain strings
     * that are not input file names (but this is unlikely).
     * 
     * @return The input file names
     */
    List<String> getInputFileNames()
    {
        return Collections.unmodifiableList(inputFileNames);
    }


}