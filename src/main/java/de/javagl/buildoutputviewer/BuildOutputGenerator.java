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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class that processes the lines of a build log, and fills a 
 * {@link BuildOutput} with the resulting information.
 */
class BuildOutputGenerator
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BuildOutputGenerator.class.getName());
    
    // Implementation note: The "payload" of a line refers to the
    // part behind the prefix "123>" (which indicates the build 
    // number that an output line belongs to)
    
    /**
     * The prefix of the payload of a line that indicates that the
     * build of a project started
     */
    private static final String BUILD_STARTED_NOTE_PREFIX = 
        "------ Build started: Project:";
    
    /**
     * The prefix of the payload of a line that indicates that the
     * project was skipped
     */
    private static final String SKIPPED_BUILD_NOTE_PREFIX = 
        "------ Skipped Build: Project:";
    
    /**
     * The prefix of the payload of a line that indicates that the
     * project was not selected to be built
     */
    private static final String PROJECT_NOT_SELECTED_NOTE_PREFIX =
        "Project not selected to build for this solution configuration";
    
    /**
     * The prefix of the payload of a line that indicates that the
     * line was generated due to the "/showIncludes" parameter 
     * being given during the build
     */
    private static final String INCLUDING_FILE_NOTE_PREFIX = 
        "  Note: including file: ";

    /**
     * The string that appears in a line payload and indicates 
     * that a compiler warning was generated
     */
    private static final String COMPILER_WARNING_NOTE = ": warning C";

    /**
     * The string that appears in a line payload and indicates 
     * that a compiler error was generated
     */
    private static final String COMPILER_ERROR_NOTE = ": error C";
    
    /**
     * The prefix of the payload of a line that indicates 
     * a linker error
     */
    private static final String LINKER_WARNING_NOTE_PREFIX =
        "???????????????????????????????????"; // TODO Haven't seen one yet...
    
    /**
     * The prefix of the payload of a line that indicates 
     * a linker error
     */
    private static final String LINKER_ERROR_NOTE_PREFIX =
        "LINK : fatal error LNK";
    
    /**
     * The string that appears in a line payload that indicates 
     * the output file of a project
     */
    private static final String PROJECT_OUTPUT_FILE_NOTE =
        ".vcxproj ->";
    
    /**
     * The prefix of the payload of a line that indicates 
     * that the line belongs to the previous error or 
     * warning
     */
    private static final String INDENTATION_PREFIX =
        "          ";
    
    /**
     * The prefix of the payload of a line that indicates
     * that code is generated. 
     */
    private static final String GENERATING_CODE_PREFIX = 
        "  Generating Code...";
    
    /**
     * The prefix of the payload of a line that indicates
     * that the compiler is compiling. Yay. 
     */
    private static final String COMPILING_PREFIX =
        "  Compiling...";
    
    /**
     * The prefix of the payload of a line that indicates
     * that the build system is checked.  
     */
    private static final String CHECKING_BUILD_SYSTEM_PREFIX =
        "  Checking Build System";
    
    /**
     * The prefix of the payload of a line that indicates
     * a CMake message 
     */
    private static final String CMAKE_DOES_NOT_NEED_TO_BE_RE_RUN_PREFIX =
        "  CMake does not need to re-run because";
    
    /**
     * The prefix of the payload of a line that indicates
     * a custom build rule message 
     */
    private static final String BUILDING_CUSTOM_RULE_PREFIX =
        "  Building Custom Rule";
    
    /**
     * The prefix of the payload of a line that indicates
     * that a library is created 
     */
    private static final String CREATING_LIBRARY_PREFIX = 
        "     Creating library";
    
    /**
     * The {@link BuildOutput} that is generated by this class
     */
    private final BuildOutput buildOutput;

    /**
     * The previous {@link Include}s that have been found in the lines
     * whose payloads start with the {@link #INCLUDING_FILE_NOTE_PREFIX}.
     * The key of this map is the indentation level (that is, the number
     * of spaces at the beginning of the payload)
     */
    private final Map<Integer, Include> previousIncludesByLevel;
    
    /**
     * The previous {@link CompilerMessage} that was generated 
     */
    private CompilerMessage previousBuildMessage;
    
    
    /**
     * Creates a new build output for the build with the given number,
     * which was extracted from the "123>" prefix of a build log line
     * 
     * @param number The build number
     */
    BuildOutputGenerator(int number)
    {
        this.buildOutput = new BuildOutput(number);
        this.previousBuildMessage = null;
        this.previousIncludesByLevel = new LinkedHashMap<Integer, Include>();
    }

    /**
     * Returns the {@link BuildOutput} that is generated by this class
     * 
     * @return The {@link BuildOutput}
     */
    BuildOutput getBuildOutput()
    {
        return buildOutput;
    }
    
    
    /**
     * Process the given line and add the resulting information 
     * to the {@link BuildOutput} that is created by this class
     * 
     * @param line The original build log line
     */
    void processLine(String line)
    {
        buildOutput.addLine(line);

        int bracketIndex = line.indexOf('>');
        if (bracketIndex == -1)
        {
            logger.warning(
                "No build number prefix found, ignoring the following line:");
            logger.warning(line);
            return;
        }
        String linePayload = line.substring(bracketIndex+1);

        // First check the prefixes that indicate ignored lines
        if (linePayload.startsWith(GENERATING_CODE_PREFIX))
        {
            return;
        }
        if (linePayload.startsWith(COMPILING_PREFIX))
        {
            return;
        }
        if (linePayload.startsWith(PROJECT_NOT_SELECTED_NOTE_PREFIX))
        {
            return;
        }
        if (linePayload.startsWith(CMAKE_DOES_NOT_NEED_TO_BE_RE_RUN_PREFIX))
        {
            return;
        }
        if (linePayload.startsWith(CHECKING_BUILD_SYSTEM_PREFIX))
        {
            return;
        }
        if (linePayload.startsWith(BUILDING_CUSTOM_RULE_PREFIX))
        {
            return;
        }
        
        if (linePayload.startsWith(CREATING_LIBRARY_PREFIX))
        {
            // TODO This could be processed somehow...
            return;
        }
        
        
        // Check for the prefixes indicating that the build started or was
        // skipped, which include the project name
        if (linePayload.startsWith(BUILD_STARTED_NOTE_PREFIX))
        {
            String projectInfo = 
                linePayload.substring(BUILD_STARTED_NOTE_PREFIX.length());
            String projectName = extractProjectName(projectInfo);
            buildOutput.setProjectName(projectName);
            return;
        }

        if (linePayload.startsWith(SKIPPED_BUILD_NOTE_PREFIX))
        {
            String projectInfo = 
                linePayload.substring(SKIPPED_BUILD_NOTE_PREFIX.length());
            String projectName = extractProjectName(projectInfo);
            buildOutput.setProjectName(projectName);
            buildOutput.setSkippedBuild(true);
            return;
        }
        
        // Check for the infix that tells the project output file name
        int projectOutputFileNoteIndex = 
            linePayload.indexOf(PROJECT_OUTPUT_FILE_NOTE);
        if (projectOutputFileNoteIndex != -1)
        {
            String fileName = linePayload.substring(
                projectOutputFileNoteIndex+PROJECT_OUTPUT_FILE_NOTE.length());
            String normalizedOutputFileName= Utils.normalizePath(fileName);
            buildOutput.setOutputFileName(normalizedOutputFileName);
            return;
        }
        
        
        // Check for the infixes that indicate compiler warnings
        int warningNoteIndex = linePayload.indexOf(COMPILER_WARNING_NOTE); 
        if (warningNoteIndex != -1)
        {
            CompilerMessage compilerMessage = 
                processCompilerMessage(linePayload);
            previousBuildMessage = compilerMessage;
            if (compilerMessage != null)
            {
                buildOutput.addCompilerWarning(compilerMessage);
            }
            return;
        }
        
        // Check for the infixes that indicate compiler errors
        int errorNoteIndex = linePayload.indexOf(COMPILER_ERROR_NOTE); 
        if (errorNoteIndex != -1)
        {
            CompilerMessage compilerMessage = 
                processCompilerMessage(linePayload);
            previousBuildMessage = compilerMessage;
            if (compilerMessage != null)
            {
                buildOutput.addCompilerError(compilerMessage);
            }
            return;
        }
        
        // Check for the infixes that indicate linker warnings
        if (linePayload.startsWith(LINKER_WARNING_NOTE_PREFIX))
        {
            // TODO The LINKER_WARNING_NOTE_PREFIX is not yet known
            LinkerMessage linkerMessage = processLinkerMessage(linePayload);
            if (linkerMessage != null)
            {
                buildOutput.addLinkerWarning(linkerMessage);
            }
            return;
        }

        // Check for the infixes that indicate linker errors
        if (linePayload.startsWith(LINKER_ERROR_NOTE_PREFIX))
        {
            LinkerMessage linkerMessage = processLinkerMessage(linePayload);
            if (linkerMessage != null)
            {
                buildOutput.addLinkerError(linkerMessage);
            }
            return;
        }
        
        // Check whether the line starts with an indentation, indicating
        // that the line is still part of the previous build message
        if (linePayload.startsWith(INDENTATION_PREFIX))
        {
            if (previousBuildMessage != null)
            {
                previousBuildMessage.addLinePayload(linePayload);
            }
            return;
        }
        
        // Check for the prefix that indicates that a "/showIncludes" 
        // line is printed, containing an included file
        if (linePayload.startsWith(INCLUDING_FILE_NOTE_PREFIX))
        {
            processIncludingFileNote(linePayload);
            return;
        }
        
        // This should always be tested last: Check if the line "probably"
        // only contains the name of a file being compiled...
        if (linePayload.startsWith("  "))
        {
            String possibleFileName = linePayload.substring(2);
            if (isProbablyFileName(possibleFileName))
            {
                buildOutput.addInputFileName(possibleFileName);
                return;
            }
        }
        
        logger.warning("Not processing the following payload:");
        logger.warning(linePayload);
    }
    
    /**
     * Returns whether the given string is probably only the
     * name of a compiled file.
     * 
     * @param string The string
     * @return Whether the string is probably only a file name
     */
    private static boolean isProbablyFileName(String string)
    {
        // A sequence of alphanumeric characters, underscores or whitespaces
        // Followed by a dot
        // Followed by "c" or "C"
        // Followed by up to two alpha characters
        String fileNameRegex = "^[\\w,\\s-]+\\.[cC][A-Za-z]{0,2}$";
        return string.matches(fileNameRegex);
    }
    

    /**
     * Extract the project name from the given string. <br>
     * <br>
     * The given string is the suffix of a line whose payload starts with the
     * {@link #BUILD_STARTED_NOTE_PREFIX} or the 
     * {@link #SKIPPED_BUILD_NOTE_PREFIX}
     * 
     * @param projectInfo The project info
     * @return The project name (this may be the full project info,
     * of the name can not be extracted)
     */
    private static String extractProjectName(String projectInfo)
    {
        int commaIndex = projectInfo.indexOf(',');
        if (commaIndex == -1)
        {
            logger.warning(
                "Could not extract project name from the following info:");
            logger.warning(projectInfo);
            return projectInfo;
        }
        return projectInfo.substring(0, commaIndex).trim();
    }

    

    /**
     * Process the information about a "warning" or an "error" from the line 
     * with the given payload, that contains the {@link #COMPILER_WARNING_NOTE} 
     * or the {@link #COMPILER_ERROR_NOTE}
     * 
     * @param linePayload The line payload
     * @return The {@link CompilerMessage}, or null if it could not be parsed
     */
    private CompilerMessage processCompilerMessage(String linePayload)
    {
        // Example format:
        // C:\file.cpp(53): error C2679: binary '=' : no operator found...
        // Could also use RegEx here...
        
        int closingBracketIndex = linePayload.indexOf("):");
        int openingBracketIndex = 
            linePayload.lastIndexOf('(', closingBracketIndex);
        if (openingBracketIndex == -1 || closingBracketIndex == -1)
        {
            logger.warning(
                "Invalid message format in the following line payload:");
            logger.warning(linePayload);
            return null;
        }
        
        int codeStartIndex = linePayload.indexOf(" C", closingBracketIndex);
        int codeEndIndex = -1;
        if (codeStartIndex != -1)
        {
            codeEndIndex = linePayload.indexOf(": ", codeStartIndex);
        }
        if (codeStartIndex == -1 || codeEndIndex == -1)
        {
            logger.warning(
                "Invalid message format in the following line payload:");
            logger.warning(linePayload);
            return null;
        }
        String codeString = 
            linePayload.substring(codeStartIndex+2, codeEndIndex);
        Integer code = Utils.tryParseInt(codeString);
        
        String fileName = linePayload.substring(0, openingBracketIndex);
        String lineNumberString = linePayload.substring(
            openingBracketIndex+1, closingBracketIndex);
        String message = linePayload.substring(closingBracketIndex+3);
        
        String normalizedFileName = Utils.normalizePath(fileName);
        Integer lineNumber = Utils.tryParseInt(lineNumberString);
        CompilerMessage buildMessage = 
            new CompilerMessage(normalizedFileName, lineNumber, code, message);
        buildMessage.addLinePayload(linePayload);
        return buildMessage;
    }
    
    
    
    /**
     * Process a {@link LinkerMessage} from the given line payload
     * 
     * @param linePayload The line payload
     * @return The {@link LinkerMessage}, or <code>null</code> if no
     * valid message could be parsed
     */
    private LinkerMessage processLinkerMessage(String linePayload)
    {
        // Example: 
        // LINK : fatal error LNK1104: cannot open file '..\foo.lib'
        int lnkIndex = linePayload.indexOf("LNK");
        int secondColonIndex = -1;
        if (lnkIndex != -1)
        {
            secondColonIndex = linePayload.indexOf(':', lnkIndex);
        }
        if (secondColonIndex == -1)
        {
            logger.warning(
                "Invalid message format in the following line payload:");
            logger.warning(linePayload);
            return null;
        }
        
        String codeString = 
            linePayload.substring(lnkIndex+3, secondColonIndex);
        Integer code = Utils.tryParseInt(codeString);
        
        String message = linePayload.substring(secondColonIndex+2);
        
        LinkerMessage linkerMessage = new LinkerMessage(code, message);
        return linkerMessage;
    }
    
    
    /**
     * Process the payload of a line that starts with the 
     * {@link #INCLUDING_FILE_NOTE_PREFIX}, and associate
     * the appropriate {@link Include} information with
     * the file name that is contained in this payload
     * 
     * @param linePayload The line payload
     */
    private void processIncludingFileNote(String linePayload)
    {
        String includeNote = 
            linePayload.substring(INCLUDING_FILE_NOTE_PREFIX.length());
        
        String includePath = includeNote.trim();
        String normalizedIncludePath = Utils.normalizePath(includePath);

        int level = computeLevel(includeNote);
        Include include = buildOutput.getInclude(normalizedIncludePath); 
        previousIncludesByLevel.put(level, include);
        if (level > 0)
        {
            Include parentInclude = previousIncludesByLevel.get(level-1);
            parentInclude.addChild(include);
            include.addParent(parentInclude);
        }
        
        previousIncludesByLevel.put(level, include);
    }

    
    /**
     * Computes the indentation level from the given string.<br>
     * <br>
     * The given string is the suffix of a line whose payload starts with the
     * {@link #INCLUDING_FILE_NOTE_PREFIX} 
     *  
     * @param includeNote The include note
     * @return The indentation level (this may be -1 if the given include
     * note does not have a valid format)
     */
    private static int computeLevel(String includeNote)
    {
        for (int i=0; i<includeNote.length(); i++)
        {
            if (includeNote.charAt(i) != ' ')
            {
                return i;
            }
        }
        return -1;
    }
}