 package com.usbsdk;
 
 import java.util.Vector;

import comon.error.DFDF1;
 
 public class BBB extends FFF
 {
   BBB(DFDF4 parameters)
   {
     super(parameters);
   }
 
   DFDF1.ERROR_CODE openPort()
   {
     return null;
   }
 
   DFDF1.ERROR_CODE closePort()
   {
     return null;
   }
 
   boolean isPortOpen()
   {
     return false;
   }
 
   DFDF1.ERROR_CODE writeData(Vector<Byte> data)
   {
     return null;
   }
   DFDF1.ERROR_CODE writeData(byte data[])
   {
     return null;
   }
   protected DFDF1.ERROR_CODE writeDataImmediately(Vector<Byte> data)
   {
     return null;
   }
 }
