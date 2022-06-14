package com.kristof.gameengine.io;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public class TwoWaySerialComm {
    private OutputStream outStream;

    public TwoWaySerialComm() {
        super();
    }

    public OutputStream getOutputStream() {
        return outStream;
    }

    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (commPort instanceof SerialPort serialPort) {
                serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                outStream = serialPort.getOutputStream();
                //(new Thread(new SerialReader(in))).start();
                //(new Thread(new SerialWriter(out))).start();
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public void write(int data) {
        try {
            outStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static class SerialReader implements Runnable {    // TODO revive
        InputStream in;
        
        public SerialReader(InputStream in) {
            this.in = in;
        }
        
        public void run () {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ( ( len = this.in.read(buffer)) > -1 ) {
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }            
        }
    }

    public static class SerialWriter implements Runnable {
        OutputStream out;
        
        public SerialWriter(OutputStream out) {
            this.out = out;
        }
        
        public void run() {
            try {
                int c = 0;
                while( (c = System.in.read()) > -1 ) {
                    this.out.write(c);
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    public static void listPorts() {
        final java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()));
        }
    }

    public static String getPortTypeName(int portType) {
        return switch (portType) {
            case CommPortIdentifier.PORT_I2C -> "I2C";
            case CommPortIdentifier.PORT_PARALLEL -> "Parallel";
            case CommPortIdentifier.PORT_RAW -> "Raw";
            case CommPortIdentifier.PORT_RS485 -> "RS485";
            case CommPortIdentifier.PORT_SERIAL -> "Serial";
            default -> "unknown type";
        };
    }
    
    /*public static void main ( String[] args ) {
        try {
            (new TwoWaySerialComm()).connect("COM3");
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }*/
}