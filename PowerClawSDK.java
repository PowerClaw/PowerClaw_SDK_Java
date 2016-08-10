/**
 * Copyright © VIVOXIE S DE RL DE CV
 *
 * @author Humberto Alonso Villegas<humberto@vivoxie.com>
 * @version 1.0
 */
package com.vivoxie.powerclaw.sdk;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
/*import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;*/

public class PowerClawSDK /*implements Runnable*/ {
    /**
     * Instance to PowerClaw
     */
    private SerialPort _serialPort = null;

    /**
     * Sensation to stop
     */
    private static String _stop = null;

    /**
     * Determine if serial port is closed or open
     */
    private static boolean _open = false;

    /**
     * Runtime sensation
     */
    private static int _timeStop;

    /**
     * Return list of the available serial ports
     * 
     * @return string
     */
    public SerialPort[] serialPortsGet()
    {
        SerialPort[] ports = SerialPort.getCommPorts();
        return ports;
    }

    /**
     * Return available PowerClaw
     * 
     * @param name
     * @return SerialPort
     */
    public SerialPort serialPortGet(String name)
    {
        if(name.equals("")) {
            name = this.defaultSerialPortGet();
        }
        /*SerialPort ports = null;
        for (SerialPort port : SerialPort.getCommPorts()) {
            if(port.getSystemPortName().equals(name)) {
                return port;
            }
            continue;
        }
        return ports;*/
        SerialPort port = SerialPort.getCommPort(name);
        return port;
    }

    /**
     * Return default serial port name by OS
     * 
     * @return String
     */
    public String defaultSerialPortGet()
    {
        if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            return "/dev/ttyUSB0";
        } else if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                Process cmd = Runtime.getRuntime().exec("cmd /c REG QUERY HKEY_LOCAL_MACHINE\\HARDWARE\\DEVICEMAP\\SERIALCOMM"); 
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
                String comm = null;
                if( (comm = stdInput.readLine()) == null) {
                    return "COM1";
                }
                while ((comm = stdInput.readLine()) != null){
                    if (comm.contains(" COM")) {
                        comm = comm.replaceAll("  ", " ");
                        String[] spl = comm.split(" ");
                        return spl[6];
                    }
                }
            } catch (IOException ioe) {
                return "COM1";
            }
            return "COM1";
        } else if(System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "/dev/tty.usbserial";
        }
        return "COM1";
    }

    /**
     * Create instance to PowerClaw
     * 
     * @param serial
     * @param baud
     * @throws com.vivoxie.powerclaw.sdk.CommException
     */
    public void serialPortSet(SerialPort serial, int baud) throws CommException
    {
        if(baud == 0){
            baud = 115200;
        }
        if(serial == null) {
            throw new CommException("PowerClaw is null");
        }
        this._serialPort = serial;
        this._serialPort.setBaudRate(baud);
    }

    /**
     * Create instance to PowerClaw
     * 
     * @throws com.vivoxie.powerclaw.sdk.CommException
     */
    public void serialPortSet() throws CommException
    {
        SerialPort serial = this.serialPortGet(this.defaultSerialPortGet());
        if(serial == null) {
            throw new CommException("PowerClaw no exist");
        }
        this._serialPort = serial;
        this._serialPort.setBaudRate(115200);
    }

    /**
     * Open communication with PowerClaw
     *
     * @return boolean
     * @throws CommException 
     */
    public boolean serialPortOpen() throws CommException
    {
        if(this._serialPort == null) {
            try {
                this.serialPortSet();
            } catch(Exception e) {
                throw new CommException("Unable connect to PowerClaw");
            }
        }
        if(PowerClawSDK._open) {
            return true;
        }
        this._serialPort.openPort();
        PowerClawSDK._open = true;
        return true;
    }

    /**
     * Send sensation to PowerClaw
     *
     * @param sensation 
     * @throws java.lang.InterruptedException 
     */
    private boolean sensationSend(String sensation) throws InterruptedException, CommException {
        String list = "";
        int intensity, phalange;
        String[] split = sensation.split(",");
        if(!PowerClawSDK._open) {
            throw new CommException("Connection to PowerClaw is closed");
        }
        switch (split[0]) {
            case "right":
                list += "r";
                break;
            case "left":
                list += "l";
                break;
            case "hands":
                list += "h";
                break;
            case "zero":
                list += "z";
                break;
            default:
                throw new CommException("Hand <" + split[0] + "> not found");
        }
        switch (split[1]) {
            case "thumb":
                list += "t";
                break;
            case "index":
                list += "i";
                break;
            case "middle":
                list += "m";
                break;
            case "ring":
                list += "r";
                break;
            case "pinkie":
                list += "p";
                break;
            case "zero":
                list += "z";
                break;
            case "hand":
                list += "h";
                break;
            default:
                throw new CommException("Finger <" + split[1] + "> not found");
        }
        try{
            phalange = Integer.parseInt(split[2]);
        } catch(Exception e) {
            throw new CommException("Phalanx <" + split[2] + "> not int");
        }
        list += split[2];
        switch (split[3]) {
            case "vibration":
                list += "v";
                break;
            case "roughness":
                list += "r";
                break;
            case "contact":
                list += "c";
                break;
            case "heat":
                list += "h";
                break;
            case "cold":
                list += "o";
                break;
            case "zero":
                list += "z";
                break;
            default:
                throw new CommException("Sensation <" + split[3] + "> not found");
        }
        try{
            intensity = Integer.parseInt(split[4]);
        } catch(Exception e) {
            throw new CommException("Intensity <" + split[3] + "> not int");
        }
        if (intensity < 0) {
            split[4] = "000";
        } else if (intensity > 100) {
            split[4] = "100";
        } else if (intensity < 10) {
            split[4] = "00" + split[4];
        } else if (intensity < 100) {
            split[4] = "0" + split[4];
        }
        list += split[4];
        list = "." + list;
        this._serialPort.writeBytes(list.getBytes(), list.length());
        return true;
    }

    /**
     * Send sensation to Power Claw with runtime
     * 
     * @param sensation
     * @param time
     * @return String
     * @throws java.lang.InterruptedException
     * @throws com.vivoxie.powerclaw.sdk.CommException
     */
    @SuppressWarnings("static-access")
    public boolean sensationSend(String sensation, int time) throws InterruptedException, CommException
    {
        String[] split = sensation.split(",");
        if(!"zero".equals(split[0]) || !"zero".equals(split[1]) || !"zero".equals(split[3])) {
            this._timeStop = time;
            this._stop = split[0] + "," + split[1] + "," + split[2] + "," + split[3] + ",000";
        } else {
            this._stop = null;
            this._timeStop = -1;
        }
        return this.sensationSend(sensation);
    }

    /**
     * Close communication with serial port
     *
     * @return boolean
     */
    public boolean serialPortClose()
    {
        if(!PowerClawSDK._open) {
            return true;
        }
        this._serialPort.closePort();
        PowerClawSDK._open = false;
        return true;
    }

    /**
     * Thread stop
     *
     *
    @Override
    public void run() {
        if(PowerClawSDK._timeStop == -1){
            return;
        }
        try {
            Thread.sleep(PowerClawSDK._timeStop);
            this.sensationSend(PowerClawSDK._stop);
        } catch (InterruptedException | CommException ex) {
            Logger.getLogger(PowerClawSDK.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
    }*/

    /**
     * Standalone
     * 
     * @param args 
     *
    static public void main(String[] args)
    {
        Comm comm = new Comm();
        SerialPort ports[];
        ports = comm.serialPortsGet();
        //ports = comm.serialPortGet("ttyUSB7");
        //System.out.println(ports.getSystemPortName());
        for (int i = 0; i < ports.length; ++i) {
            System.out.println("[" + i + "] " + ports[i].getSystemPortName() + ": " + ports[i].getDescriptivePortName());
        }
        System.out.print("\nChoose your second desired serial port, or enter -1 to skip this test: ");
        try {
            comm.serialPortSet(ports[(new Scanner(System.in)).nextInt()], 115200); 
            comm.serialPortOpen();
            //comm.sensationSend("right,ring,0,heat,100", 1000);
        } catch (Exception e) {
            System.out.println("error");
        }
        try {
            comm.sensationSend("right,hand,0,vibration,95", 2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Comm.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            comm.sensationSend("left,hand,0,vibration,95", 3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Comm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
}