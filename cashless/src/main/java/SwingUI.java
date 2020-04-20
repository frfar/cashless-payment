import java.awt.event.*;
import javax.swing.*;

import keypad.UIKeypad;
import net.miginfocom.swing.*;
/*
 * Created by JFormDesigner on Thu Dec 05 00:05:20 CST 2019
 */



/**
 * @author unknown
 */
public class SwingUI extends JPanel {

    public SwingUI(){
        initComponents();
        list1.setListData(new String[]{"lays 101 $2", "doritos 102 $2", "cup cake 103 $3"});
    }

    public SwingUI(boolean displayList) {
        initComponents();
        if (displayList) {
            list1.setListData(new String[]{"lays 101 $2", "doritos 102 $2", "cup cake 103 $3"});
        }
    }

    public void setMessageLabel(String text) {
        messageLabel.setText(text);
        messageLabel.invalidate();
    }

    private void button1ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("1");
    }

    private void button2ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("2");
    }

    private void button3ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("3");
    }

    private void button4ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("4");
    }

    private void button5ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("5");
    }

    private void button6ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("6");
    }

    private void button7ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("7");
    }

    private void button8ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("8");
    }

    private void button9ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("9");
    }

    private void button10ActionPerformed(ActionEvent e) {
        UIKeypad.addKeyStroke("0");
    }

    private void buttonEnterActionPerformed(ActionEvent e){
        UIKeypad.addKeyStroke("\n");
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        messageLabel = new JLabel();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        list1 = new JList();
        button4 = new JButton();
        button5 = new JButton();
        button6 = new JButton();
        button7 = new JButton();
        button8 = new JButton();
        button9 = new JButton();
        button10 = new JButton();
        buttonEnter = new JButton();

        //======== this ========
        setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing
        . border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn", javax. swing. border. TitledBorder
        . CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("Dia\u006cog" ,java .
        awt .Font .BOLD ,12 ), java. awt. Color. red) , getBorder( )) )
        ;  addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
        ) {if ("\u0062ord\u0065r" .equals (e .getPropertyName () )) throw new RuntimeException( ); }} )
        ;
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]"));

        //---- messageLabel ----
        messageLabel.setText("Please Swipe the Card:");
        add(messageLabel, "cell 0 0 5 1,alignx center,growx 0");

        //---- button1 ----
        button1.setText("1");
        button1.addActionListener(e -> button1ActionPerformed(e));
        add(button1, "cell 0 2");

        //---- button2 ----
        button2.setText("2");
        button2.addActionListener(e -> button2ActionPerformed(e));
        add(button2, "cell 2 2");

        //---- button3 ----
        button3.setText("3");
        button3.addActionListener(e -> button3ActionPerformed(e));
        add(button3, "cell 4 2");
        add(list1, "cell 5 2 1 4");

        //---- button4 ----
        button4.setText("4");
        button4.addActionListener(e -> button4ActionPerformed(e));
        add(button4, "cell 0 4");

        //---- button5 ----
        button5.setText("5");
        button5.addActionListener(e -> button5ActionPerformed(e));
        add(button5, "cell 2 4");

        //---- button6 ----
        button6.setText("6");
        button6.addActionListener(e -> button6ActionPerformed(e));
        add(button6, "cell 4 4");

        //---- button7 ----
        button7.setText("7");
        button7.addActionListener(e -> button7ActionPerformed(e));
        add(button7, "cell 0 6");

        //---- button8 ----
        button8.setText("8");
        button8.addActionListener(e -> button8ActionPerformed(e));
        add(button8, "cell 2 6");

        //---- button9 ----
        button9.setText("9");
        button9.addActionListener(e -> button9ActionPerformed(e));
        add(button9, "cell 4 6");

        //---- button10 ----
        button10.setText("0");
        button10.addActionListener(e -> button10ActionPerformed(e));
        add(button10, "cell 2 7");

        buttonEnter.setText("Enter");
        buttonEnter.addActionListener(e -> buttonEnterActionPerformed(e));
        add(buttonEnter, "cell 4 7");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JLabel messageLabel;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JList list1;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    private JButton button7;
    private JButton button8;
    private JButton button9;
    private JButton button10;
    private JButton buttonEnter;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
