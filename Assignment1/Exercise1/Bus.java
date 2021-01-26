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
import java.util.*;
import java.util.function.*;

/**
 * Main Bus JavaBean class.
 *
 * Implements the main Bus logic and properties.
 *
 * @author Andrea Laretto
 */
public class Bus implements Serializable {

    /**
     * String constants identifying the properties on this bean.
     */
    public static final String PROP_CAPACITY      = "capacity";
    public static final String PROP_NUMPASSENGERS = "numPassengers";
    public static final String PROP_DOOROPEN      = "doorOpen";

    /**
     * The number of passengers will randomly decrease every 10 seconds.
     */
    public static final int SLEEP_TIME = 10000;

    /**
     * The doors will remain open for 3 seconds.
     */
    public static final int DOORS_WAIT = 3000;

    /**
     * Initial values of the main properties of the bean.
     */
    private int     capacity      = 50;
    private int     numPassengers = 20;
    private boolean doorOpen      = false;

    /**
     * The thread delaying the increase in passengers while waiting for the
     * doors to be open at least DOORS_WAIT seconds.
     */
    private Thread numPassengersDelayThread = null;

    /**
     * Standard list of (vetoable) listeners waiting for changes on this bean.
     */
    private final PropertyChangeSupport listeners     = new PropertyChangeSupport(this);
    private final VetoableChangeSupport vetoListeners = new VetoableChangeSupport(this);

    /**
     * Lock to make numPassengers a thread-safe property.
     */
    private final Object numPassengersLock = new Object();

    public Bus() {
        // All variables have been initialized with their specified values.
        // The empty constructor is required for this class to be considered a
        // valid JavaBean, along with the implementation of Serializable.
    }

    /**
     * Activate the bus so that every SLEEP_TIME seconds it will try to decrease
     * the number of passengers by a random positive amount.
     */
    public void activate() {
        Random random = new Random();
        (new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(SLEEP_TIME);
                    // Decreasing on a random integer in [0, numPassengers + 1]
                    // makes it so that the updated value will not be negative.
                    // Since numPassengers is constructed as a thread-safe property,
                    // along with the updateNumPassengers function, there is no
                    // risk that the value will change after having calculated
                    // the next randomly decreased value to be set.
                    updateNumPassengers(p -> p - random.nextInt(p + 1));
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }

    // Simple property getter for the doorOpen property
    public boolean isDoorOpen() {
        return this.doorOpen;
    }

    // Simple property setter for the doorOpen property
    public void setDoorOpen(boolean newDoorOpen) {
        boolean oldDoorOpen = this.doorOpen;
        this.doorOpen = newDoorOpen;
        listeners.firePropertyChange(PROP_DOOROPEN, oldDoorOpen, newDoorOpen);
    }

    // Simple property getter for the doorOpen property
    public int getCapacity() {
        return this.capacity;
    }

    // Simple property setter for the capacity property
    public void setCapacity(int newCapacity) {
        int oldCapacity = this.capacity;
        this.capacity = newCapacity;
        listeners.firePropertyChange(PROP_CAPACITY, oldCapacity, newCapacity);
    }

    // Simple property setter for the numPassengers property
    public int getNumPassengers() {
        return this.numPassengers;
    }

    // Simple property getter for the numPassengers property, using an update function
    public void setNumPassengers(int v) {
        updateNumPassengers(__ -> v);
    }

    /**
     * @param f The updating function that will receive the
     *          previous value of numPassengers and return the next one to be
     *          set as value of the property
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateNumPassengers(Function<Integer, Integer> f) {
        // numPassengers must be set as a thread-safe property, since multiple threads
        // will be trying to modify it: the GUI events and the periodic decreaser.
        // We can implement a synchronized "fmap"-like (in the Haskell sense)
        // function to atomically perform a sort of "getAndSet" operation on
        // the value of the passengers.

        // A Runnable encapsulating the listener notification to send
        // at the end of this method, if present.
        Runnable notifyListenersAtEnd = null;

        synchronized(numPassengersLock) {
            // First, get the old number of passengers.
            int oldNumPassengers = this.numPassengers;
            // Apply the provided function inside the synchronized block.
            int newNumPassengers = f.apply(oldNumPassengers);

            // Check if the new number respects the bus capacity that it's non-negative;
            // otherwise, don't even notify the listeners and return failure.
            if(!(0 <= newNumPassengers && newNumPassengers < getCapacity()))
                return false;
            else
                try {
                    // First, try to notify the vetoers. This is the only
                    // point where the PropertyVetoException can be raised.
                    vetoListeners.fireVetoableChange(PROP_NUMPASSENGERS, oldNumPassengers, newNumPassengers);
                    // If we are here it means that the change passed,
                    // so we can safely notify the standard listeners.

                    // We have two cases: either passengers are coming down from the
                    // back door or coming up from the front door.
                    // First, determine if passengers are coming off or on the bus.
                    int passengersChange = newNumPassengers - oldNumPassengers;

                    // If they are coming up, we need to keep the door open for
                    // at least 3 seconds before making the change. Otherwise,
                    // the passengers can simply come off the bus at any time.
                    // At the end, we can in both cases notify the proper listeners.
                    if(passengersChange > 0)
                        // Queue the update until after the doors will have remained
                        // open for at least 3 seconds.
                        queueWaitingDoors(() -> {
                            // Add or subtract the number of passengers and notify the listeners.
                            // Re-synchronize the change after the delay has passed
                            // so that we can still perform atomic updates.
                            // Note that there can only be one update at a time.
                            synchronized(numPassengersLock) {
                                this.numPassengers += passengersChange;
                            }
                            // Immediately notify the listeners outside of the synchronized block.
                            listeners.firePropertyChange(PROP_NUMPASSENGERS, oldNumPassengers, this.numPassengers);
                        });
                    else {
                        // Simply let the passengers come off.
                        this.numPassengers += passengersChange;
                        // At the end of the method, and outside of the
                        // synchronized block, notify the listeners.
                        notifyListenersAtEnd = () -> {
                            listeners.firePropertyChange(PROP_NUMPASSENGERS, oldNumPassengers, this.numPassengers);
                        };
                    }
                } catch(PropertyVetoException e){
                    // If we are here it means that the change has been vetoed.
                    // This also means that the variable has not been modified and
                    // that the standard listeners have also not been notified.
                    return false;
                }
        }
        // Everything went well, return success and notify the listeners at
        // the end of the synchronized block if necessary.
        // The standard listeners are updated outside of the synchronized block
        // in case one of them tried to recursively call this method and cause
        // the lock to be taken again. Furthermore, it might be that the listeners
        // take a long time before completion, thus weighing down on the thread-safe property.
        if(notifyListenersAtEnd != null)
            notifyListenersAtEnd.run();
        return true;
    }

    // This method is internal to updateNumPassengers and is declared as private.
    // Note that this method immediately terminates and is non-blocking. This is
    // so that simple getters and setters on this property behave as usually
    // expected from a simple property, thus transparently hiding the delay
    // internal mechanism from the caller of the setter method.
    private void queueWaitingDoors(Runnable f) {
        // If there is no other pending update,
        if(this.numPassengersDelayThread == null) {
            // Instantiate and fill the thread variable with a new thread,
            this.numPassengersDelayThread = new Thread(() -> {
                try {
                    setDoorOpen(true); // Open the doors

                    Thread.sleep(DOORS_WAIT); // Wait 3 seconds

                    f.run(); // Finally execute the update

                    setDoorOpen(false); // Close the doors

                    // Finally, remove the thread object so that the
                    // update method can be called again.
                    this.numPassengersDelayThread = null;
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            });
            // and finally start the thread.
            this.numPassengersDelayThread.start();
        }
        // else, the requested update is ignored, since passengers are already
        // coming on the bus. Due to COVID we shouldn't allow too many people
        // at the same time from the same door! :-)

        // Note that only one instance of the thread can be running at a time.
        // We do not rely on the fact that external callers will not attempt
        // to call the updateNumPassengers function twice while the opening
        // and closing doors process is ongoing, thus we always check for
        // previous updates.
    }

    // Standard functions to add a (vetoable) listener on a certain property of this bean.
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(property, listener);
    }
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(property, listener);
    }
    public void addPropertyChangeListener(String property, VetoableChangeListener listener) {
        vetoListeners.addVetoableChangeListener(property, listener);
    }
    public void removePropertyChangeListener(String property, VetoableChangeListener listener) {
        vetoListeners.addVetoableChangeListener(property, listener);
    }

    // This method simply sends an initialization event to all the non-vetoable
    // listeners registered to the model, using null as precedent value.
    // This can be useful to update the listeners to the initial state, in such a
    // way that the controller nor the view have to *directly* interact with the
    // model getters and setters; instead, all the relevant information is directly
    // passed by the event listeners already registered.
    public void refreshPropertyListeners() {
        listeners.firePropertyChange(PROP_DOOROPEN,      null, isDoorOpen());
        listeners.firePropertyChange(PROP_CAPACITY,      null, getCapacity());
        listeners.firePropertyChange(PROP_NUMPASSENGERS, null, getNumPassengers());
    }
}
