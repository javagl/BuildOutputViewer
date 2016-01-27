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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The class managing the mappings between compiler- or linker error
 * or warning codes, and the corresponding MSDN links
 */
class LinkMappings
{
    /**
     * The mappings of compiler warnings
     */
    private static final Properties compilerWarningLinks;

    /**
     * The mappings of compiler errors
     */
    private static final Properties compilerErrorLinks;

    /**
     * The mappings of compiler fatal errors
     */
    private static final Properties compilerFatalErrorLinks;

    /**
     * The mappings of linker warnings
     */
    private static final Properties linkerWarningLinks;

    /**
     * The mappings of linker errors
     */
    private static final Properties linkerErrorLinks;

    /**
     * Load the properties from the resource with the given name
     * 
     * @param name The resource name
     * @return The properties
     */
    private static Properties load(String name)
    {
        Properties properties = new Properties();
        try (InputStream inputStream = 
            LinkMappings.class.getResourceAsStream(name))
        {
            if (inputStream == null)
            {
                System.err.println("Resource not found: "+name);
            }
            else
            {
                properties.load(inputStream);
                inputStream.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return properties;
    }
    
    // Static initialization
    static
    {
        compilerWarningLinks = load("/compilerWarningLinks.txt");        
        compilerErrorLinks = load("/compilerErrorLinks.txt");        
        compilerFatalErrorLinks = load("/compilerFatalErrorLinks.txt");        
        linkerWarningLinks = load("/linkerWarningLinks.txt");        
        linkerErrorLinks = load("/linkerErrorLinks.txt");        
    }
    
    /**
     * Returns the link to the MSDN page explaining the given code 
     * 
     * @param code The code. The string representation of this object
     * will be used for the lookup.
     * @return The link, or <code>null</code> if no matching link is found
     */
    static String getCompilerWarningLink(Object code)
    {
        return (String)compilerWarningLinks.get(String.valueOf(code));
    }

    /**
     * Returns the link to the MSDN page explaining the given code 
     * 
     * @param code The code. The string representation of this object
     * will be used for the lookup.
     * @return The link, or <code>null</code> if no matching link is found
     */
    static String getCompilerErrorLink(Object code)
    {
        return (String)compilerErrorLinks.get(String.valueOf(code));
    }

    /**
     * Returns the link to the MSDN page explaining the given code 
     * 
     * @param code The code. The string representation of this object
     * will be used for the lookup.
     * @return The link, or <code>null</code> if no matching link is found
     */
    static String getCompilerFatalErrorLink(Object code)
    {
        return (String)compilerFatalErrorLinks.get(String.valueOf(code));
    }

    /**
     * Returns the link to the MSDN page explaining the given code 
     * 
     * @param code The code. The string representation of this object
     * will be used for the lookup.
     * @return The link, or <code>null</code> if no matching link is found
     */
    static String getLinkerWarningLink(Object code)
    {
        return (String)linkerWarningLinks.get(String.valueOf(code));
    }

    /**
     * Returns the link to the MSDN page explaining the given code 
     * 
     * @param code The code. The string representation of this object
     * will be used for the lookup.
     * @return The link, or <code>null</code> if no matching link is found
     */
    static String getLinkerErrorLink(Object code)
    {
        return (String)linkerErrorLinks.get(String.valueOf(code));
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    LinkMappings()
    {
        // Private constructor to prevent instantiation
    }
    
}
