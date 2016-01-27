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
import java.util.Objects;

/**
 * An instance of a compiler message (Warning or Error) that was found in 
 * the build log
 */
class CompilerMessage
{
    /**
     * The normalized file name of where the message appeared
     */
    private final String normalizedFileName;
    
    /**
     * The line number of the message, if it could be parsed
     */
    private final Integer lineNumber;
    
    /**
     * The error or warning code of the message, if it could be parsed
     */
    private final Integer code;
    
    /**
     * The message string
     */
    private final String message;
    
    /**
     * Further lines of the output that belong to this message
     */
    private final List<String> linePayloads;
    
    /**
     * Create a new instance
     * 
     * @param normalizedFileName The normalized file name
     * @param lineNumber The line number
     * @param code The error code
     * @param message The message
     */
    CompilerMessage(String normalizedFileName, 
        Integer lineNumber, Integer code, String message)
    {
        this.normalizedFileName = normalizedFileName;
        this.lineNumber = lineNumber;
        this.code = code;
        this.message = message;
        this.linePayloads = new ArrayList<String>();
    }
    
    /**
     * Returns the normalized file name of where this message appeared
     * 
     * @return The file name
     */
    String getNormalizedFileName()
    {
        return normalizedFileName;
    }
    
    /**
     * Returns the line number of where this message appeared, if this
     * line number could be parsed - otherwise, <code>null</code> is returned
     * 
     * @return The line number
     */
    Integer getLineNumber()
    {
        return lineNumber;
    }
    
    /**
     * Returns the error/warning code for this message, if it could be 
     * parsed - otherwise, <code>null</code> is returned
     * 
     * @return The error/warning code
     */
    Integer getCode()
    {
        return code;
    }
    
    /**
     * Returns the actual message
     * 
     * @return The message
     */
    String getMessage()
    {
        return message;
    }
    
    /**
     * Add the given line payload to the list of lines of the output
     * that are associated with this message
     * 
     * @param linePayload The line payload
     */
    void addLinePayload(String linePayload)
    {
        linePayloads.add(linePayload);
    }
    
    /**
     * Returns the unmodifiable list of line payloads for this message.
     * This list at least contains the actual line that contains the
     * reason of why this message was created.
     *  
     * @return The line payloads
     */
    List<String> getLinePayloads()
    {
        return Collections.unmodifiableList(linePayloads);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(
            normalizedFileName, lineNumber, code, message, linePayloads);
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null)
        {
            return false;
        }
        if (getClass() != object.getClass())
        {
            return false;
        }
        CompilerMessage other = (CompilerMessage) object;
        if (!Objects.equals(normalizedFileName, other.normalizedFileName))
        {
            return false;
        }
        if (!Objects.equals(lineNumber, other.lineNumber))
        {
            return false;
        }
        if (!Objects.equals(code, other.code))
        {
            return false;
        }
        if (!Objects.equals(message, other.message))
        {
            return false;
        }
        if (!Objects.equals(linePayloads, other.linePayloads))
        {
            return false;
        }
        return true;
    }
    
}