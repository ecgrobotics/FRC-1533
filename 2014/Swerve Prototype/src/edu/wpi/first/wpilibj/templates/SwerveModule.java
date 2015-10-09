/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.*;

/**
 *
 * @author Programming
 */
public class SwerveModule {
    AnalogChannel encoder;
    Jaguar drive, steer;
    
    public SwerveModule(int driveModule, int drivePin, int steerModule, int steerPin, int encoderModule, int encoderPin) {
        drive = new Jaguar(driveModule, drivePin);
        steer = new Jaguar(steerModule, steerPin);
        encoder = new AnalogChannel(encoderModule, encoderPin);
    }
    
    public SwerveModule(int drivePin, int steerPin, int encoderPin) {
        this(1, drivePin, 1, steerPin, 1, encoderPin);
    }
    
    
}
