/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.ADXL345_I2C;
import edu.wpi.first.wpilibj.DigitalModule;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SensorBase;

/**
 *
 * @author Programming
 */
public class ADXL345 extends SensorBase {
    ADXL345_I2C acc=new ADXL345_I2C(1,ADXL345_I2C.DataFormat_Range.k16G);
    private static final byte ADDRESS = 58;
    private static final byte POWER_CTL_REGISTER = 0x2D;//45;
    private static final byte kDataFormatRegister = 49;
    private static final byte kDataRegister = 50;
    private static final double kGsPerLSB = 0.00390625;
    private static final byte kPowerCtl_Link = 32;
    private static final byte kPowerCtl_AutoSleep = 16;
    private static final byte kPowerCtl_Measure = 8;
    private static final byte kPowerCtl_Sleep = 4;
    private static final byte kDataFormat_SelfTest = -128;
    private static final byte kDataFormat_SPI = 64;
    private static final byte kDataFormat_IntInvert = 32;
    private static final byte kDataFormat_FullRes = 8;
    private static final byte kDataFormat_Justify = 4;
    public static final byte G2=0;
    public static final byte G4=2;
    public static final byte G8=3;
    public static final byte G16=4;
    private I2C i2c;
    
    public ADXL345(int slot,byte resolution){
        DigitalModule module=DigitalModule.getInstance(slot);
        i2c=module.getI2C(ADDRESS);
    }
    
    
    
    
}

