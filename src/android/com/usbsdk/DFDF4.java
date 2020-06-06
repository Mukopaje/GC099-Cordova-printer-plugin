package com.usbsdk;

import android.content.Context;
import java.net.InetAddress;

import comon.error.DFDF1;

public class DFDF4
{
  public DFDF1.PORT_TYPE PortType;
  public String PortName;
  public int PortNumber;
  public String IPAddress;
  public char DeviceID;
  public String DeviceName;
  public Context ApplicationContext;

  public DFDF4()
  {
    this.PortType = DFDF1.PORT_TYPE.ETHERNET;
    this.PortName = "";
    this.PortNumber = 9100;
    this.IPAddress = "192.168.192.168";
    this.DeviceID = '\000';
    this.DeviceName = "";
    this.ApplicationContext = null;
  }

  public DFDF1.ERROR_CODE validateParameters()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    switch (this.PortType)
    {
    case SERIAL:
      break;
    case PARALLEL:
      break;
    case USB:
      if (this.ApplicationContext == null)
      {
        retval = DFDF1.ERROR_CODE.INVALID_APPLICATION_CONTEXT;
      }
      break;
    case ETHERNET:
      if (this.PortNumber <= 0)
      {
        retval = DFDF1.ERROR_CODE.INVALID_PORT_NUMBER;
      }
      else if (this.IPAddress.length() != 0)
      {
        try
        {
          InetAddress.getByName(this.IPAddress);
        }
        catch (Exception e)
        {
          retval = DFDF1.ERROR_CODE.INVALID_IP_ADDRESS;
        }

      }
      else
      {
        retval = DFDF1.ERROR_CODE.INVALID_IP_ADDRESS;
      }
      break;
    default:
      retval = DFDF1.ERROR_CODE.INVALID_PORT_TYPE;
    }

    return retval;
  }

  public DFDF4 copy()
  {
    DFDF4 dp = new DFDF4();
    dp.PortType = this.PortType;
    dp.PortName = this.PortName;
    dp.PortNumber = this.PortNumber;
    dp.IPAddress = this.IPAddress;
    dp.DeviceID = this.DeviceID;
    dp.DeviceName = this.DeviceName;
    dp.ApplicationContext = this.ApplicationContext;

    return dp;
  }
}
