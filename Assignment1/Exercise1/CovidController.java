/*
 * MIT License
 *
 * Copyright (c) 2020 Andrea Laretto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ap.assignment1.exercise1;

import java.beans.*;
import java.io.*;

/**
 * Main CovidController JavaBean class.
 * 
 * Acts both as a JavaBean with a reducedCapacity property and as a 
 * concrete VetoableChangeListeners that can be attached to integer properties
 * in order to limit to a certain amount smaller than reducedProperty.
 * 
 * @author Andrea Laretto
 */
public class CovidController implements Serializable, VetoableChangeListener {
        
    /**
     * PropertyVetoException requires a message indicating the veto reason.
     */
    public static final String VETO_MESSAGE = "The capacity exceeds the reducedCapacity imposed by CovidController.";
    
    /**
     * String constants identifying the properties on this bean.
     */
    public static final String PROP_REDUCEDCAPACITY = "reducedCapacity";
    
    /**
     * Initial property of the bean.
     */
    private int reducedCapacity = 25;
    
    /**
     * Simple supporting class for possible listeners on the reducedCapacity property.
     */
    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    
    public CovidController() {
        // All variables have been initialized with their specified values.
        // The empty constructor is required for this class to be considered a
        // valid JavaBean, along with the implementation of Serializable.
    }
    
    // Simple property getter for reducedCapacity
    public int getReducedCapacity() {
        return this.reducedCapacity;
    }

    // Simple property setter for the reducedCapacity
    public void setReducedCapacity(int newReducedCapacity) {
        int oldReducedCapacity = this.reducedCapacity;
        this.reducedCapacity = newReducedCapacity;
        listeners.firePropertyChange(PROP_REDUCEDCAPACITY, oldReducedCapacity, newReducedCapacity);
    }
    
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(property, listener);
    }
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(property, listener);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        // This vetoer does not make any assumption on the name of the property
        // being checked for, as long as it can be cast to Integer.
        // This allows for this bean to be reused in different context with different
        // property names, while maintaining the same "thresholding" semantics.
        if(evt.getNewValue() instanceof Integer && (Integer)evt.getNewValue() > getReducedCapacity())
            throw new PropertyVetoException(VETO_MESSAGE, evt);
    }
    
    // Read Bus::refreshPropertyListeners for the same explanation.
    // This method is provided for completeness, even though reducedCapacity
    // has not (yet) any interface element that can modify it.
    public void refreshPropertyListeners() {
        listeners.firePropertyChange(PROP_REDUCEDCAPACITY, null, getReducedCapacity());
    }
}