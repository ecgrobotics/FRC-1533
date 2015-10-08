package org.usfirst.frc.team1533.robot;

//import javax.swing.Timer;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.SerialPort.WriteBufferMode;
public class LaunchPad{
	public SerialPort port;
	boolean done=false;
	DriverStation ds=DriverStation.getInstance();
	volatile int[] values=new int[4];//static size, use ports[] for index
	public volatile boolean to=false;
	Thread updater=new Thread(){
		public void run(){
			Timer t=new Timer();
			t.start();
			//TODO Disregard all values is 1  >4096
			int[] tmp=new int[values.length];
			long last=System.currentTimeMillis();
			out:
			while(!done){
				try{
					while(!ds.isEnabled());
					t.reset();
					while(port.getBytesReceived()>0)port.read(port.getBytesReceived());
					port.write(new byte[] {1}, 1);
	    			while(port.getBytesReceived()<=7){
	    				if(t.get()>.02){
//	    					System.out.println("TIMEOUT");
	    					to=true;
	    					continue out; //TODO UNCOMMENT?
	    				}
	    			}
	    			to=false;
	    			byte[] b=port.read(8);
	    		//	System.out.println(Long.toBinaryString(b[4])+" "+Long.toBinaryString(b[5])+",  "+Long.toBinaryString(b[6])+" "+Long.toBinaryString(b[7]));
	    			for(int i=0;i<4;i++){
	    				tmp[i]=(Byte.toUnsignedInt(b[i*2])<<8)+Byte.toUnsignedInt(b[i*2+1]);
	    				//System.out.print(values[i]+ "   ,   ");
	    			}  
	    			for(int i:tmp){
	    				if(i>4096){
	    					System.err.println("GOT SOM BAD VALUES!");
	    					continue out;
	    				}
	    			}
	    			for(int i=0;i<tmp.length;i++){
	    				values[i]=tmp[i];
	    			}
	    			try{
	    				Thread.sleep(10);
	    			}
	    			catch(Exception e){
	    				
	    			}
				}catch(Exception e){
					for(int i=0;i<values.length;i++){
						values[i]=0;
					}
				}
			}
		}
	};
	
	/**
	 * 
	 * @param ports which analog ports you are interested in
	 */
	public LaunchPad(){
		port=new SerialPort(115200,SerialPort.Port.kMXP,8, SerialPort.Parity.kNone,SerialPort.StopBits.kTwo);
		port.reset();
		port.setWriteBufferMode(WriteBufferMode.kFlushOnAccess);
	//?	port.setReadBufferSize(8);
		port.setWriteBufferSize(1);
		updater.start();
		
	}
	public void free(){
		done=true;
		port.flush();
		port.free();
	}
	/**
	 * 
	 * @param port 0-7
	 * @return
	 */
	public int getAnalog(int port){
		return values[port];
	}

}
