package com.example.machinedata;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.util.Log;

/**
 * Receiver class for UDP packets containing 64-bit double values.
 * @author Tomi Leinonen
 *
 */
public class UdpReceiver {
	
	private DatagramSocket clientSocket;
    private byte[] receiveData;
    private int signCount;
    private final int timeout = 1;
    
    /**
     * Create a receiver.
     * @param port Port to receive from.
     * @param signCount Number of signals in a packet
     */
    public UdpReceiver(int port, int signCount){
    	this.signCount = signCount;
    	
    	//byte[] ipAddress = {(byte) 192, (byte) 168, (byte) 0, (byte) 7};
    	receiveData = new byte[8 * signCount];
    	try{
    		clientSocket=new DatagramSocket(port);
    		clientSocket.setReceiveBufferSize(64*signCount);
//    		try {
//				clientSocket.connect(InetAddress.getByAddress(ipAddress), port);
//			} catch (UnknownHostException e) {
//				Log.e("ERR", "HostException in UdpReceiver()");
//			}
    		clientSocket.setSoTimeout(timeout);
		}catch(SocketException e){
			Log.e("ERR", "SocketException in UdpReceiver()");
		}
    }
    
    /**
     * Receive a data packet and split it into array.
     * @param data Array to put data in, must be correct size
     * @return True on successful read, false otherwise
     */
    public boolean receive(double[] data){
    	//receive a packet
    	DatagramPacket recvPacket = new DatagramPacket(receiveData, receiveData.length);
	    try{
	    	clientSocket.receive(recvPacket);
	    }catch(IOException e){
	    	Log.e("ERR", "IOException in UdpReceiver.receive()");
	    	return false;
	    }
	    
	    //convert packet into double array
	    if(data.length < signCount){
	    	return false;
	    }
	    for(byte b: receiveData){
	    	Log.e("LOG", String.valueOf(b));
	    }
	    byte[] reverse = new byte[signCount * 8];
	    
	    int j = 0;
	    for(int i = signCount * 8 - 1; i >= 0; i--){
	    	reverse[j] = receiveData[i];
	    	j++;
	    }
	    ByteBuffer bytes = ByteBuffer.wrap(reverse);
	    for(int i = 0; i < signCount; i++){
	    	data[signCount - (i+1)] = bytes.getDouble(i*8);
	    	Log.e("LOG", String.valueOf(data[i]));
	    }

	    return true;
    }
}
