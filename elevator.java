package OSPROJECT;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class elevator extends Frame implements ActionListener {
    private int currentFloor = 0;
    private TextArea resultArea;
    private ArrayList<Integer> floorRequests = new ArrayList<>();
    private boolean movingUp = true;

    public elevator() {
        setTitle("Elevator Simulation with SCAN Algorithm");
        setSize(300, 400);
        setLayout(new FlowLayout());

        for (int i = 0; i <= 10; i++) {
            Button floorButton = new Button("Floor " + i);
            floorButton.setActionCommand(String.valueOf(i));
            floorButton.addActionListener(this);
            add(floorButton);
        }

        resultArea = new TextArea(10, 25);
        resultArea.setEditable(false);
        add(resultArea);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int targetFloor = Integer.parseInt(e.getActionCommand());
        if (!floorRequests.contains(targetFloor)) {
            floorRequests.add(targetFloor);
            resultArea.append("Floor " + targetFloor + " added to requests.\n");
            processRequests();
        }
    }

    private void processRequests() {
        StringBuilder result = new StringBuilder();
        result.append("Current Floor: ").append(currentFloor).append("\n");


        while (!floorRequests.isEmpty()) {
       
            Collections.sort(floorRequests); 
            if (movingUp) {
                for (int i = 0; i < floorRequests.size(); i++) {
                    if (floorRequests.get(i) >= currentFloor) {
                        moveToFloor(floorRequests.get(i), result); 
                        floorRequests.remove(i);
                        i--; 
                    }
                }
                movingUp = false; 
            } else {
                for (int i = floorRequests.size() - 1; i >= 0; i--) {
                    if (floorRequests.get(i) <= currentFloor) {
                        moveToFloor(floorRequests.get(i), result);
                        floorRequests.remove(i);
                    }
                }
                movingUp = true;
            }
        }
     

        resultArea.setText(result.toString());
    }

    private void moveToFloor(int targetFloor, StringBuilder result) {
        if (currentFloor < targetFloor) {
            for (int i = currentFloor + 1; i <= targetFloor; i++) {
                result.append("Passing Floor: ").append(i).append("\n");
            }
        } else {
            for (int i = currentFloor - 1; i >= targetFloor; i--) {
                result.append("Passing Floor: ").append(i).append("\n");
            }
        }
        currentFloor = targetFloor;
        result.append("Arrived at Floor: ").append(currentFloor).append("\n");
    }

    public static void main(String[] args) {
        new elevator();
    }
}