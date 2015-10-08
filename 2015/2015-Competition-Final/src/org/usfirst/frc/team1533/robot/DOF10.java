package org.usfirst.frc.team1533.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.InterruptHandlerFunction;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer.Range;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

public class DOF10 extends SensorBase /*implements LiveWindowSendable*/ {//maybe later when i know how it works...
	private I2C acc;
	private I2C comp;
	private I2C press;
	private double accres=0;
	private double gyrores=0;
	private double compres=100.0/1090.0;//makes it uT
	
	private int oss=1;
	//compass mode
	private static final int ONE_SAMPLE=1;
	private static final int CONTINUOUS=0;
	private static final int CONFIG=0x1A;
	private static final int IDLE=2;
	private DigitalInput interrupt=null;
	private DigitalInput drdy=null;
	private Thread gyroThread;
	private int AC1,AC2,AC3,B1,B2,MB,MC,MD, AC4,AC5,AC6;
	private double[] off=new double[3];
	public DOF10(I2C.Port port){
		this(port,-1,-1);
	}
	public DOF10(I2C.Port port, int interrupt, int drdy) {
		this.interrupt=interrupt>-1?new DigitalInput(interrupt):null;
		this.drdy=drdy>-1?new DigitalInput(drdy):null;
		acc=new I2C(port,0x68);		
		byte[] b=new byte[1];
		acc.read(0x6b,1,b);
		acc.write(0x6b,b[0]&0b10111111);//wake up!
		acc.read(55,1,b);
		acc.write(55, b[0]|0b00000010);//make compass visible
		comp=new I2C(port,0x1e);
		press=new I2C(port,0x77);
		setAccelerometerRange(Range.k2G);
		setGyroRange(GyroRange.k500);
		//TODO set accelerometer interrupt to data_ready
		//check bit 6 reg 36, needs to be 0
		acc.write(56, 1);//set interrupt to data_ready
	//	this.setLPF(Frequency.k95);
		byte[] b2=new byte[1];
		comp.read(0, 1, b2);
		comp.write(0, 0b11111011 & b2[0]);
		setCompassMode(drdy>-1?ONE_SAMPLE:CONTINUOUS);
		setPressureResolution(Resolution.ULTRA_HIGH);
		AC1=readRaw(press,0xAA);
		AC2=readRaw(press,0xAC);
		AC3=readRaw(press,0xAE);
		AC4=readRawUnsigned(press,0xB0);
		AC5=readRawUnsigned(press,0xB2);
		AC6=readRawUnsigned(press,0xB4);
		B1=readRaw(press,0xB6);
		B2=readRaw(press,0xB8);
		MB=readRaw(press,0xBA);
		MC=readRaw(press,0xBC);
		MD=readRaw(press,0xBE);
		
		calibrate();
		
	}
	public void calibrate(){
		double[] sample=new double[3];
		for(int i=0;i<25;i++){
			double[] s=getAngularRate();
			for(int j=0;j<3;j++){
				sample[j]+=s[j];
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(int i=0;i<3;i++){
			off[i]=sample[i]/25.0;
			System.out.println(off[i]);
		}
	}
	private volatile boolean gyroStop=false;
	private volatile boolean gyroPause=false;
	private volatile double yaw=0,dyaw=0.0;
	private volatile double roll=0;
	private volatile double pitch=0;
	public double getdYaw(){
		return dyaw;
	}
	public void startGyroIntegrator(){
		if(gyroThread!=null){
			if(gyroThread.isAlive())return;
			gyroThread=null;
		}
		gyroThread=new Thread("Gyro Integrator"){
			public void run(){
				while(!gyroStop){
					long last=System.currentTimeMillis();//nanoTime();
					while(!gyroPause){
						double[] d=getAngularRate();
						long temp=0;
						double dif=(temp=System.currentTimeMillis())-last;
						last=temp;
						dif*=1e-3;
					//	System.out.println(dif);
						pitch+=dif*d[0];//pitch about x
						roll+=dif*d[1];//roll about y
						
						dyaw=d[2];
						yaw+=dyaw*dif;//yaw about y
						try{
							Thread.sleep(1);//max refresh is 1KHz, dont go over
						}catch(Exception e){
							
						}
					}
				}
			}
		};
		gyroThread.start();		
	}
	public void pauseIntegrator(){
		gyroPause=true;
	}
	public void resumeIntegrator(){
		gyroPause=false;
	}
	public void stopIntegrator(){
		gyroStop=true;
	}
	public double getYaw(){
		return yaw;
	}
	public void resetYaw(){
		yaw=0;
	}
	public double getPitch(){
		return pitch;
	}
	public double getRoll(){
		return roll;
	}
	private int readRawUnsigned(I2C sensor,int add){
		byte[] b=new byte[2];
		sensor.read(add, 2, b);
		int d=(Byte.toUnsignedInt(b[0])<<8) + Byte.toUnsignedInt(b[1]);
		return d;
	}
	private int readRaw(I2C sensor,int add){
		byte[] b=new byte[2];
		sensor.read(add, 2, b);
		int d=(Byte.toUnsignedInt(b[0])<<8) + Byte.toUnsignedInt(b[1]);
		if(d>=0x8000)d=-((65535-d)+1);
		//if(sensor==comp)setMode(ONE_SAMPLE);
		return d;
	}
	private int[] readAllRaw(I2C sensor,int add){
		byte[] b=new byte[6];
		sensor.read(add, 6, b);
		int[] d=new int[3];
		for(int i=0;i<6;i+=2){
			d[i/2]=(Byte.toUnsignedInt(b[i])<<8)|Byte.toUnsignedInt(b[i+1]);
			if(d[i/2]>=0x8000)d[i/2]=-((65535-d[i/2])+1);
		}		
		return d;
	}
	public double accConvert(double d){
		return d/accres;
	}
	public double gyroConvert(double d){
		return d/gyrores;
	}
	public double compConvert(double d){
		return d*compres;
	}
	public double getAX() {
		return accConvert(readRaw(acc,0x3b));
	}
	public double getAY() {
		return accConvert(readRaw(acc,0x3d));
	}
	public double getAZ() {
		return accConvert(readRaw(acc,0x3f));
	}
	public double[] getAcceleration(){
		int[] d=readAllRaw(acc,0x3b);
		double[] out=new double[d.length];
		for(int i=0;i<d.length;i++){
			out[i]=accConvert(d[i]);
		}
		return out;
	}
	public double getGX(){
		return gyroConvert(readRaw(acc,0x43));
	}
	public double getGY(){
		return gyroConvert(readRaw(acc,0x45));
	}
	public double getGZ(){
		return gyroConvert(readRaw(acc,0x47));
	}
	public double[] getAngularRate(){
		int[] d=readAllRaw(acc,0x43);
		double[] out=new double[d.length];
		for(int i=0;i<d.length;i++){
			out[i]=gyroConvert(d[i])-off[i];
		}
		return out;
	}
	public double getCX(){
		return compConvert(readRaw(comp,3));
	}
	public double getCY(){
		return compConvert(readRaw(comp,7));
	}
	public double getCZ(){
		return compConvert(readRaw(comp,5));
	}
	public double[] getCompass(){
		int[] d=readAllRaw(comp,3);
		double[] out=new double[d.length];
		for(int i=0;i<d.length;i++){
			out[i]=compConvert(d[i]);
		}
		double t=out[2];//compass is x,z,y
		out[2]=out[1];
		out[1]=t;
		return out;
	}
	public double getPressure(){
		press.write(0xF4, 0x2E);
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long UT=readRaw(press,0xF6);//the ds said long, but its 16bit...
		long X1= (long) ((UT-AC6)*AC5/Math.pow(2, 15));
		long X2=(int) (MC*Math.pow(2, 11)/(X1+MD));
		long B5=X1+X2;
		press.write(0xF4,0x34+(oss<<6));
		byte[] b=new byte[1];
		while((b[0]&0b00100000)>0){press.read(0xF4,1,b);}//while the conversion bit is high (not ready)
		byte[] b3=new byte[3];
		press.read(0xF6,3,b3);
		long UP=(Byte.toUnsignedLong(b3[0])<<16+Byte.toUnsignedInt(b3[1])<<8+Byte.toUnsignedInt(b3[2]))>>(8-oss);
		long B6=B5-4000;
		X1=(long) ((B2*(B6*B6/Math.pow(2,12)))/Math.pow(2,11));
		X2=(long) (AC2*B6/Math.pow(2,11));
		long X3=X1+X2;
		long B3=(((AC1*4+X3)<<oss)+2)/4;
		X1=(long) (AC3*B6/Math.pow(2,13));
		X2=(long) ((B1*(B6*B6/Math.pow(2,12)))/Math.pow(2,16));
		X3=((X1+X2)+2)/4;
		long B4=(long) (AC4*(long)(X3+32768)/Math.pow(2,15));
		long B7=((long)UP-B3)*(50000>>oss);
		long p=0;
		if(B7<(long)0x80000000){
			p=(B7*2)/B4;
		}
		else{
			p=(B7/B4)*2;
		}
		X1=(long) Math.pow((p/Math.pow(2,8)),2);
		X1*=3038;
		X1/=Math.pow(2,16);
		X2=(long) ((-7357*p)/Math.pow(2,16));
		p+=(X1+X2+3791)/Math.pow(2, 4);
		return p/100.0;
				
		
	}
	public double getTemperature(){
		press.write(0xF4, 0x2E);
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int UT=readRaw(press,0xF6);//the ds said long, but its 16bit...
		int X1=(int) ((UT-AC6)*AC5/Math.pow(2, 15));
		int X2=(int) (MC*Math.pow(2, 11)/(X1+MD));
		int B5=X1+X2;
		return (B5+8)/Math.pow(2, 4)/10.0;
	}
	public void setAccelerometerRange(Range r){
		int data=0;
		switch(r){
		case k4G:accres=8192.0;data=0b01000;break;
		case k8G:accres=4096.0;data=0b10000;break;
		case k16G:accres=2048.0;data=0b11000;break;
		default:
			accres=16384.0;data=0b00000;break;
		}
		acc.write(28, data);
	}
	public void setGyroRange(GyroRange r){
		int data=0;
		
		switch(r){
		case k250:data=0;gyrores=131;break;
		case k500:data=1;gyrores=65.5;break;
		case k1000:data=2;gyrores=32.8;break;
		case k2000:data=3;gyrores=16.4;break;
		}
		acc.write(27, data<<3);
	}
	public void setCompassRange(CompassRange r){
		//TODO
	}
	
	private void setCompassMode(int mode){
		comp.write(2, mode);
	}
	
	public void setLPF(Frequency f){
		byte[] b=new byte[1];
		acc.read(CONFIG, 1, b);
		int i=0;
		switch(f){
		case k260:i=0;break;
		case k190:i=1;break;
		case k95:i=2;break;
		case k45:i=3;break;
		case k20:i=4;break;
		case k10:i=5;break;
		case k5:i=6;break;
		}
		acc.write(CONFIG, b[0]& (0b11111000 | i));
	}
	public void setPressureResolution(Resolution r){
		switch(r){
		case LOW:oss=0;break;
		case STANDARD:oss=1;break;
		case HIGH:oss=2;break;
		case ULTRA_HIGH:oss=3;break;
		}
		byte[] b=new byte[1];
		press.read(0xF4, 1,b);
		press.write(0xF4,b[0]&(0b00111111 | oss<<6));
	}
	public enum Frequency{k260,k190,k95,k45,k20,k10,k5}
	public enum GyroRange{k250,k500,k1000,k2000};
	public enum Resolution{LOW,STANDARD,HIGH,ULTRA_HIGH};
	public enum CompassRange{};
	//public DMP dmp;
	/*
	public class DMP{
		volatile boolean dmpStop=false;
		volatile boolean dmpPause=false;
		volatile boolean dmpInit=false;
		volatile double ax,ay,az,gx,gy,gz,cx,cy,cz,p,t,roll,pitch,yaw,head,inc,ss,alt=0.0;
		double ax0,ay0,az0,dip0,head0,p0=0.0;
		Thread dmp,at,ct;//dmp- pressure,temp?,ct-compass,at-acc/gyro
		private DMP(){//no instances. :P sorry, not sorry
		}
		public void init(){
			double[] as=new double[3];
			double[] tmp=new double[3];
			double samples=50.0;
			for(int i=0;i<samples;i++){
				tmp=getAcceleration();
				for(int j=0;j<3;j++){
					as[j]+=tmp[j];
				}
				interrupt.waitForInterrupt(.01);
				//todo may need to read reg 58 to clear interrupt
			}
			ax0=as[0]/samples;
			ay0=as[1]/samples;
			az0=as[2]/samples;
			
			samples=10;
			double[] cs=new double[3];
			for(int i=0;i<samples;i++){
				tmp=getAcceleration();
				for(int j=0;j<3;j++){
					cs[j]+=tmp[j];
				}
				interrupt.waitForInterrupt(.01);
				//todo may need to read reg 58 to clear interrupt
			}
			for(int i=0;i<3;i++){
				cs[i]/=samples;
			}
			//TODO calc heading & dip
			
			
			dmpInit=true;
		}
		public void start(){
			if(!dmpInit){
				System.err.println("DMP NOT INITIALIZED! Call DMP.init()");
				return;
			}
			if(!dmpStop){
				dmpStop=true;
				//TODO make this stop the threads and recreate
				return;
			}
			dmp=new Thread("DMP Thread 1"){
				public void run(){
					
					while(!dmpStop){
						while(!dmpPause){
							
						}
					}
				}
			};
			//dmp.start();
			at=new Thread("DMP Thread 2"){
				public void run(){
					NSTimer time=new NSTimer();
					time.start();
					while(!dmpStop){
						while(!dmpPause){
							
							//may need to read reg 58 to clear interrupt?
							interrupt.waitForInterrupt(.05);//refresh rate is 1kHz, adjust based on delay from filtering
							
							time.restart();
						}
						time.restart();
					}
				}
			};
			//at.start();
			ct=new Thread("DMP Thread 3"){
				public void run(){
					NSTimer time=new NSTimer();
					time.start();
					while(!dmpStop){
						
						while(!dmpPause){
							drdy.waitForInterrupt(.025);//max wait time is 25ms (ultra high res)
							time.restart();
						}
						time.restart();
					}
				}
			};
		//	ct.start();
		}
	}
	*/
}
