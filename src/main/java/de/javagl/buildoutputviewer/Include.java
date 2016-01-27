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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A class summarizing the information that was extracted from the
 * build log include information, which is created when a file
 * is compiled with the "/showIncludes" option.
 */
class Include
{
    /**
     * The normalized path of the included file
     */
    private final String normalizedPath;
    
    /**
     * The children of this include. That is, the includes that
     * are included by this file
     */
    private final Set<Include> children;

    /**
     * The parents of this include. That is, the includes that
     * include this file
     */
    private final Set<Include> parents;
    
    /**
     * Create a new include object with the given normalized file path
     * 
     * @param normalizedPath The normalized file path
     */
    Include(String normalizedPath)
    {
        this.normalizedPath = normalizedPath;
        this.children = new LinkedHashSet<Include>();
        this.parents = new LinkedHashSet<Include>();
    }
    
    /**
     * Add the given parent to this include. This is an include that
     * includes this include
     * 
     * @param parent The parent
     */
    void addParent(Include parent)
    {
        parents.add(parent);
    }
    
    /**
     * Add the given parent to this include. This is an include that
     * this include includes
     * 
     * @param child The child
     */
    void addChild(Include child)
    {
        children.add(child);
    }
    
    /**
     * Returns an unmodifiable set containing the children of this include.
     * These are the includes that this include includes
     * 
     * @return The children
     */
    Set<Include> getChildren()
    {
        return Collections.unmodifiableSet(children);
    }
    
    /**
     * Returns an unmodifiable set containing the parents of this include.
     * These are the includes that include this include
     * 
     * @return The children
     */
    Set<Include> getParents()
    {
        return Collections.unmodifiableSet(parents);
    }

    /**
     * Returns the normalized path describing the included file
     * 
     * @return The normalized path
     */
    String getNormalizedPath()
    {
        return normalizedPath;
    }
    
    @Override
    public String toString()
    {
        return getNormalizedPath();
    }
    
}