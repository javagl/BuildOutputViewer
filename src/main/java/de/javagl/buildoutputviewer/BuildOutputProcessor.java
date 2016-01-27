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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntConsumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The class that processes the output of one build. It receives the lines
 * of the build output as a list of strings in the 
 * {@link #process(List, IntConsumer)} method, and offers the results of
 * processing the build output as a list of {@link BuildOutput} objects.
 */
class BuildOutputProcessor
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(BuildOutputProcessor.class.getName());
    
    /**
     * The prefix of a line that indicates the summary of the build output
     */
    private static final String BUILD_SUMMARY_PREFIX = 
        "========== Build:";
    
    /**
     * The mapping from build numbers (the numbers in the "123>" prefixes
     * of the lines) to the {@link BuildOutputGenerator} that generates
     * the corresponding {@link BuildOutput} for all lines with the
     * respective prefix
     */
    private final Map<Integer, BuildOutputGenerator> buildOutputGenerators;
    
    /**
     * The set of lines that have been ignored and not dispatched to
     * any {@link BuildOutputGenerator}
     */
    private final List<String> ignoredLines;
    
    /**
     * The number of "succeeded" builds, according to the output that is
     * processed in {@link #processSummary(String)}
     */
    private Integer numSucceeded = null;

    /**
     * The number of "failed" builds, according to the output that is
     * processed in {@link #processSummary(String)}
     */
    private Integer numFailed = null;

    /**
     * The number of "up-to-date builds", according to the output that is
     * processed in {@link #processSummary(String)}
     */
    private Integer numUpToDate = null;

    /**
     * The number of "skipped" builds, according to the output that is
     * processed in {@link #processSummary(String)}
     */
    private Integer numSkipped = null;
    
    /**
     * Default constructor
     */
    BuildOutputProcessor()
    {
        this.buildOutputGenerators = 
            new TreeMap<Integer, BuildOutputGenerator>();
        this.ignoredLines = new ArrayList<String>();
    }
    
    /**
     * Returns an unmodifiable list containing the {@link BuildOutput} objects
     * that are created by this class. The list will contain <i>references</i>
     * to the {@link BuildOutput} objects. 
     * 
     * @return The generated {@link BuildOutput}s
     */
    Collection<BuildOutput> getBuildOutputs()
    {
        return Collections.unmodifiableList(
            buildOutputGenerators.values().stream()
                .map(b -> b.getBuildOutput())
                .collect(Collectors.toList()));
    }
    
    /**
     * Returns the number of builds that "succeeded" according to the
     * output, or <code>null</code> if not project summary could be parsed
     * 
     * @return The number
     */
    Integer getNumSucceeded()
    {
        return numSucceeded;
    }
    
    /**
     * Returns the number of builds that "failed" according to the
     * output, or <code>null</code> if not project summary could be parsed
     * 
     * @return The number
     */
    Integer getNumFiled()
    {
        return numFailed;
    }
    
    
    /**
     * Returns the number of builds that are "up-to-date" according to the
     * output, or <code>null</code> if not project summary could be parsed
     * 
     * @return The number
     */
    Integer getNumUpToDate()
    {
        return numUpToDate;
    }
    
    
    /**
     * Returns the number of builds that are "skipped" according to the
     * output, or <code>null</code> if not project summary could be parsed
     * 
     * @return The number
     */
    Integer getNumSkipped()
    {
        return numSkipped;
    }
    
    
    /**
     * Reset this processor to its initial state
     */
    void clear()
    {
        buildOutputGenerators.clear();
        ignoredLines.clear();
    }
    
    /**
     * Process the given list of lines, containing a build log. The given
     * list may not be modified while this method is running. 
     * 
     * @param lines The lines
     * @param lineCounterCallback An optional callback that will be informed
     * about each line that was processed
     * @return Whether the list was processed completely. If the thread
     * executing this method was interruped, then <code>false</code> is
     * returned
     */
    boolean process(List<String> lines, IntConsumer lineCounterCallback)
    {
        for (int i=0; i<lines.size(); i++)
        {
            String line = lines.get(i);
            //System.out.println(line);
            processLine(line);
            
            if (Thread.currentThread().isInterrupted())
            {
                return false;
            }
            if (lineCounterCallback != null)
            {
                lineCounterCallback.accept(i);
            }
        }
        return true;
    }

    /**
     * Process a single line from a build output. This will usually dispatch
     * the given line to the {@link BuildOutputGenerator}, depending on the
     * "123>" prefix that indicates the build number.
     * 
     * @param line The line
     */
    private void processLine(String line)
    {
        if (line.startsWith(BUILD_SUMMARY_PREFIX))
        {
            processSummary(line);
            return;
        }
        
        int bracketIndex = line.indexOf('>');
        if (bracketIndex == -1)
        {
            logger.warning(
                "No build number prefix found, ignoring the following line:");
            logger.warning(line);
            ignoredLines.add(line);
            return;
        }
        
        String buildNumberString = line.substring(0, bracketIndex);
        Integer buildNumber = Utils.tryParseInt(buildNumberString);
        if (buildNumber == null)
        {
            logger.warning(
                "No valid build number found, ignoring the following line:");
            logger.warning(line);
            ignoredLines.add(line);
            return;
        }
        
        BuildOutputGenerator buildOutputGenerator = 
            buildOutputGenerators.get(buildNumber);
        if (buildOutputGenerator == null)
        {
            buildOutputGenerator = new BuildOutputGenerator(buildNumber);
            buildOutputGenerators.put(buildNumber, buildOutputGenerator);
        }
        
        //System.out.println("BuildNumber "+buildNumber+" in "+line);
        buildOutputGenerator.processLine(line);
    }
    
    /**
     * Process the given line that summarizes the build output (usually,
     * the last line of the build output), including information about
     * how many succeeded/failed/up-to-date/skipped projects there are
     * 
     * @param line The summary line
     */
    private void processSummary(String line)
    {
        // Example:
        // ========== Build: 40 succeeded, 1 failed, 6 up-to-date, 6 skipped
        numSucceeded = tryParseIntegerBefore(line, "succeeded");
        numFailed = tryParseIntegerBefore(line, "failed");
        numUpToDate = tryParseIntegerBefore(line, "up-to-date");
        numSkipped = tryParseIntegerBefore(line, "skipped");
    }
    
    /**
     * Try to parse the integer that appears right before the given suffix
     * in the given input. Returns <code>null</code> if no matching integer
     * can be parsed
     * 
     * @param input The input
     * @param suffix The suffix after the integer
     * @return The integer
     */
    private static Integer tryParseIntegerBefore(String input, String suffix)
    {
        Pattern p = Pattern.compile("(\\d+) "+suffix);
        Matcher m = p.matcher(input);
        if (m.find()) 
        {
            return Utils.tryParseInt(m.group(1));
        }        
        return null;
    }
    
    
    
}