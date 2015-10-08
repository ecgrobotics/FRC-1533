package org.usfirst.frc.team1533.robot;


import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;

public class ADC {
	/*
	 * adc.writeBulk(new byte[]{0x40});
    	byte[] b=new byte[1];
    	adc.readOnly(b, 1);
        System.out.println(Byte.toUnsignedInt(b[0]));
	 */
	I2C adc;
	Thread t;
	volatile int[] values=new int[4];
	DriverStation ds=DriverStation.getInstance();
	public static int DELAY=50;
	public ADC(I2C.Port port){
		adc=new I2C(port,0x48);
		t=new Thread("ADC Thread"){
			public void run(){
				long start=0;
				while(true){
					start=System.currentTimeMillis();
					byte[] b=new byte[1];
					for(byte i=0;i<4;i++){
						adc.writeBulk(new byte[]{(byte) (i+0x40+1)});
						adc.readOnly(b, 1);
						values[i]=Byte.toUnsignedInt(b[0]);
					}
					long wait=50-(System.currentTimeMillis()-start);
					if(wait>0)try{Thread.sleep(wait);}catch(Exception e){}
				}
			}
		};
		t.start();
	}
	
	public int getAnalog(int i){
		return values[i];
	}
}
