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

import java.util.Objects;

/**
 * An instance of a linker message (Warning or Error) that was found in 
 * the build log
 */
class LinkerMessage
{
    /**
     * The error or warning code of the message, if it could be parsed
     */
    private final Integer code;
    
    /**
     * The message string
     */
    private final String message;
    
    /**
     * Create a new instance
     * 
     * @param code The error code
     * @param message The message
     */
    LinkerMessage(Integer code, String message)
    {
        this.code = code;
        this.message = message;
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
    
    @Override
    public int hashCode()
    {
        return Objects.hash(code, message);
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
        LinkerMessage other = (LinkerMessage) object;
        if (!Objects.equals(code, other.code))
        {
            return false;
        }
        if (!Objects.equals(message, other.message))
        {
            return false;
        }
        return true;
    }
    
}