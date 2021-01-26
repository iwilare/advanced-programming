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

import java.awt.Color;
import java.beans.*;
import java.util.function.*;

/**
 * Main BusBoard graphical interface class.
 * 
 * @author Andrea Laretto
 */
public class BusBoard extends javax.swing.JFrame {

    /**
     * Amount of time to wait before trying to open the door for new passengers.
     */
    private static final int ACCEPTING_WAIT = 2000;
    
    /**
     * A simple thread variable that delays the opening of doors by the amount
     * specified by the ACCEPTING_WAIT delay.
     */
    private Thread buttonDelayThread = null;

    /**
     * Standard empty constructor for the BusBoard, initializing both the
     * graphical form and the properties associated.
     * Note that the other JavaBeans required for this class have been added
     * to this class using the graphical interface of NetBeans, thus demonstrating
     * that even non-graphical JavaBeans can be added with the same drag-and-drop
     * feature available for other components. The instance variables representing
     * the Bus and the CovidController instances can be found below the user-defined
     * methods, in the automatically generated section of this class at the end.
     */
    public BusBoard() {
        initComponents();
        initProperties();
        
        // Activate the periodic function to let passengers get down.
        bus.activate();
    }
    
    // Initialize the properties of the JavaBeans used and attach the View
    // to the Model using the listeners, thus allowing for low coupling 
    // between the two.
    final void initProperties() {        
        // These helper functions abstract the concrete way the view is updated and managed.
        
        Consumer<Boolean> busDoorOpenUpdate                    = v -> DoorOpen.setText(v ? "Yes" : "No");
        Consumer<Boolean> busDoorOpenColorUpdate               = v -> DoorOpen.setForeground(v ? new Color(0, 153, 0) : Color.red);
        Consumer<Integer> busCapacityUpdate                    = v -> Capacity.setText(v.toString());
        Consumer<Integer> busNumPassengersUpdate               = v -> NumPassengers.setText(v.toString());
        Consumer<Integer> covidControllerReducedCapacityUpdate = v -> ReducedCapacity.setText(v.toString());
        
        // We are in the main MVC controller: connect the Bus model properties
        // in order to update the View when the model notifies its listeners of changes.
        // The listeners are simple wrappers on the view functions.
        
        PropertyChangeListener busDoorOpenListener = e -> {
            busDoorOpenUpdate.accept((Boolean)e.getNewValue());
            busDoorOpenColorUpdate.accept((Boolean)e.getNewValue());
        };
        PropertyChangeListener busCapacityListener = e ->
            busCapacityUpdate.accept((Integer)e.getNewValue());
        PropertyChangeListener busNumPassengersListener = e ->
            busNumPassengersUpdate.accept((Integer)e.getNewValue());

        PropertyChangeListener covidControllerReducedCapacityListener = e ->
            covidControllerReducedCapacityUpdate.accept((Integer)e.getNewValue());
        
        // Add the CovidController as VetoableChangeListener in order to
        // automatically limit the maximum number of passengers.
        
        bus.addPropertyChangeListener(Bus.PROP_NUMPASSENGERS, covidController);
        
        // Add the remaining property listeners and attach them to the model.
        
        bus.addPropertyChangeListener(Bus.PROP_DOOROPEN, busDoorOpenListener);
        bus.addPropertyChangeListener(Bus.PROP_CAPACITY, busCapacityListener);
        bus.addPropertyChangeListener(Bus.PROP_NUMPASSENGERS, busNumPassengersListener);
        
        covidController.addPropertyChangeListener(
            CovidController.PROP_REDUCEDCAPACITY,
            covidControllerReducedCapacityListener);
        
        // At the beginning of the program, the view must be updated according
        // to the initial values of the program, which we obviously cannot set
        // directly from the GUI since it ought to be separate from the model.
        // There are two choices here:
        // - either notify the listeners with a fake event constructed ad-hoc,
        // - or use the already-abstracted view functions to update the system.
        // We can choose to directly ask the model to call the event handlers
        // with a starting update that simply indicates initialization.
        // This allows us to avoid having to directly interact with the model getters
        // to obtain all the relevant information; we ask instead the model to give
        // us the updated information for all the relevant listeners we already added.
        
        bus.refreshPropertyListeners();
        covidController.refreshPropertyListeners();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bus = new com.ap.assignment1.exercise1.Bus();
        covidController = new com.ap.assignment1.exercise1.CovidController();
        NumPassengers = new javax.swing.JLabel();
        DoorOpen = new javax.swing.JLabel();
        Enter = new javax.swing.JButton();
        LabelNumPassengers = new javax.swing.JLabel();
        LabelDoorOpen = new javax.swing.JLabel();
        LabelAddPassengers = new javax.swing.JLabel();
        AddNumPassengers = new javax.swing.JSpinner();
        LabelCapacity = new javax.swing.JLabel();
        LabelReducedCapacity = new javax.swing.JLabel();
        Capacity = new javax.swing.JLabel();
        ReducedCapacity = new javax.swing.JLabel();
        Info1 = new javax.swing.JLabel();
        Info2 = new javax.swing.JLabel();
        Info3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Exercise 1 - Bus Board");

        NumPassengers.setText("?");

        DoorOpen.setText("?");

        Enter.setBackground(java.awt.Color.white);
        Enter.setText("Add passengers");
        Enter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EnterMouseClicked(evt);
            }
        });
        Enter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EnterActionPerformed(evt);
            }
        });

        LabelNumPassengers.setText("Number of passengers:");

        LabelDoorOpen.setText("Is the door open?");

        LabelAddPassengers.setText("Add number of passengers:");

        AddNumPassengers.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5, 1));
        AddNumPassengers.setMaximumSize(new java.awt.Dimension(1, 5));

        LabelCapacity.setText("Bus capacity: ");

        LabelReducedCapacity.setText("Reduced bus capacity: ");

        Capacity.setText("?");

        ReducedCapacity.setText("?");

        Info1.setText("The number of passengers will be randomly decreased every " + Bus.SLEEP_TIME/1000 + " seconds.");

        Info2.setText("The door will remain open for " + Bus.DOORS_WAIT/1000 + " seconds.");

        Info3.setText("The door will try to open after " + ACCEPTING_WAIT/1000 + " seconds of pressing the request button.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Enter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelAddPassengers)
                            .addComponent(LabelDoorOpen)
                            .addComponent(LabelNumPassengers)
                            .addComponent(LabelCapacity)
                            .addComponent(LabelReducedCapacity))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Capacity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(NumPassengers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ReducedCapacity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DoorOpen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AddNumPassengers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(Info2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Info3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Info1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(12, 12, 12)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelNumPassengers)
                    .addComponent(NumPassengers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelDoorOpen)
                    .addComponent(DoorOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelCapacity)
                    .addComponent(Capacity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelReducedCapacity)
                    .addComponent(ReducedCapacity))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelAddPassengers)
                    .addComponent(AddNumPassengers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addComponent(Info2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Info3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Info1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Enter, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void EnterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EnterMouseClicked
        Enter.setBackground(Color.red);
        Enter.setText("(waiting " + ACCEPTING_WAIT/1000 + " seconds before trying to open the door)");
        
        // Using a similar mechanism as the Bus class, check that the
        // button has not been pressed twice, in order not to spawn a thread 
        // every single time that the button gets pressed.
        
        // If this button has not been pressed already,
        if(this.buttonDelayThread == null) {     
            // Associate the button delay thread with a new instance,
            this.buttonDelayThread = new Thread(() -> {
                try {
                    // Wait 2 seconds, then send the update that will
                    // later wait for the incoming door to open.

                    Thread.sleep(ACCEPTING_WAIT);

                    // Use the thread-safe updateNumPassenger function to directly
                    // perform a sort of atomic "getAndSet" operation on the model.
                    // The value obtained from the JSpinner component is already bounded
                    // with an appropriate structural setting on the component itself,
                    // allowing only an Integer value with a [1,5] interval constraint.

                    bus.updateNumPassengers(n -> n + (Integer)AddNumPassengers.getValue());
                } catch(InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // In any case, revert the button and the value selector to their defaults.
                    Enter.setBackground(Color.white);
                    Enter.setText("Add passengers");
                    AddNumPassengers.setValue(1);
                    
                    // Set the variable associated with this thread to null, so
                    // that the button can be called again.
                    this.buttonDelayThread = null;
                }
            });
            // and start running the thread with the delay.
            this.buttonDelayThread.start();
        }
    }//GEN-LAST:event_EnterMouseClicked

    private void EnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EnterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_EnterActionPerformed
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BusBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BusBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BusBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BusBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BusBoard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner AddNumPassengers;
    private javax.swing.JLabel Capacity;
    private javax.swing.JLabel DoorOpen;
    private javax.swing.JButton Enter;
    private javax.swing.JLabel Info1;
    private javax.swing.JLabel Info2;
    private javax.swing.JLabel Info3;
    private javax.swing.JLabel LabelAddPassengers;
    private javax.swing.JLabel LabelCapacity;
    private javax.swing.JLabel LabelDoorOpen;
    private javax.swing.JLabel LabelNumPassengers;
    private javax.swing.JLabel LabelReducedCapacity;
    private javax.swing.JLabel NumPassengers;
    private javax.swing.JLabel ReducedCapacity;
    private com.ap.assignment1.exercise1.Bus bus;
    private com.ap.assignment1.exercise1.CovidController covidController;
    // End of variables declaration//GEN-END:variables
}
