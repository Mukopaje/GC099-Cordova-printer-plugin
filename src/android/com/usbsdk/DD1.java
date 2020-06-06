package com.usbsdk;


 

import java.util.Date;
import java.util.Vector;

import comon.error.DFDF1;

public class DD1
{
  private DFDF4 m_DeviceParameters;
//  private GpComScanParameters m_ScanParameters = new GpComScanParameters();
  private ASBStatus m_ASBStatus = new ASBStatus();
  private FFF m_Port;
  private A2 m_callback = null;

  public DD1()
  {
    this.m_DeviceParameters = null;
    this.m_Port = null;
    this.m_callback = null;
  }

  public DFDF1.ERROR_CODE setDeviceParameters(DFDF4 params)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    retval = params.validateParameters();
    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      this.m_DeviceParameters = params.copy();
    }

    return retval;
  }

  public DFDF1.ERROR_CODE openDevice()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (!isDeviceOpen().booleanValue())
    {
      if (this.m_DeviceParameters != null)
      {
        retval = this.m_DeviceParameters.validateParameters();
        if (retval == DFDF1.ERROR_CODE.SUCCESS)
        {
          switch (this.m_DeviceParameters.PortType)
          {
          case SERIAL:
            break;
          case PARALLEL:
            break;
          case USB:
            CCCC.d("GpComDevice", "Creating USB port...");
            this.m_Port = new GGG(this.m_DeviceParameters);
            break;
          case ETHERNET:
            CCCC.d("GpComDevice", "Creating Ethernet port...");
            this.m_Port = new DDDD(this.m_DeviceParameters);
            break;
          case BLUETOOTH:
            CCCC.d("GpComDevice", "Creating Bluetooth port...");
            this.m_Port = new BBB(this.m_DeviceParameters);
            break;
          default:
            retval = DFDF1.ERROR_CODE.INVALID_PORT_TYPE;
          }

          if ((this.m_Port != null) && (retval == DFDF1.ERROR_CODE.SUCCESS))
          {
            CCCC.d("GpComDevice", "Port creation successful");

            if (this.m_callback != null)
            {
              retval = this.m_Port.registerCallback(this.m_callback);
            }

            if (retval == DFDF1.ERROR_CODE.SUCCESS)
            {
              retval = this.m_Port.openPort();
              if (retval == DFDF1.ERROR_CODE.SUCCESS)
              {
              }

            }
          }
          else
          {
            CCCC.d("GpComDevice", "Port creation NOT successful");
            retval = DFDF1.ERROR_CODE.FAILED;
          }
        }
      }
      else
      {
        retval = DFDF1.ERROR_CODE.NO_DEVICE_PARAMETERS;
      }
    }
    else
    {
      retval = DFDF1.ERROR_CODE.DEVICE_ALREADY_OPEN;
    }

    return retval;
  }

  public DFDF1.ERROR_CODE closeDevice()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (isDeviceOpen().booleanValue())
    {
      retval = this.m_Port.closePort();
      if (retval == DFDF1.ERROR_CODE.SUCCESS)
      {
        this.m_Port = null;
      }
      else
      {
        retval = DFDF1.ERROR_CODE.FAILED;
      }
    }

    return retval;
  }

  public Boolean isDeviceOpen()
  {
    if (this.m_Port != null)
    {
      return Boolean.valueOf(this.m_Port.isPortOpen());
    }

    return Boolean.valueOf(false);
  }

  public DFDF1.ERROR_CODE sendData(Vector<Byte> data)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port != null)
    {
      retval = this.m_Port.writeData(data);
    }
    else
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    return retval;
  }
  

  public DFDF1.ERROR_CODE sendCommand(String command)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port != null)
    {
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }
    else
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    return retval;
  }

  public Vector<Byte> getReceivedData()
  {
    Vector receivedData = null;

    if (this.m_Port != null)
    {
      receivedData = this.m_Port.readData();
    }

    return receivedData;
  }

  public DFDF1.ERROR_CODE requestDeviceStatus(int type, Boolean waitforanswer, int timeout)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    String command = "";

    if (this.m_Port == null)
    {
      return DFDF1.ERROR_CODE.FAILED;
    }

    switch (type)
    {
    case 0:
      command = "DLE EOT 0 1";
      break;
    case 1:
      command = "DLE EOT 1";
      break;
    case 2:
      command = "DLE EOT 2";
      break;
    case 3:
      command = "DLE EOT 3";
      break;
    case 4:
      command = "DLE EOT 4";
      break;
    case 5:
      command = "DLE EOT 5";
      break;
    case 6:
      command = "DLE EOT 8 1";
      break;
    case 7:
      command = "DLE EOT 8 2";
      break;
    default:
      retval = DFDF1.ERROR_CODE.INVALID_DEVICE_STATUS_TYPE;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if ((retval == DFDF1.ERROR_CODE.SUCCESS) && (waitforanswer.booleanValue()))
    {
      Date NowDate = new Date();
      Date TimeoutDate = new Date(NowDate.getTime() + timeout * 1000);

      while ((!this.m_Port.isRealtimeStatusAvailable().booleanValue()) && (NowDate.before(TimeoutDate)))
      {
        try
        {
          Thread.sleep(100L);
        } catch (InterruptedException localInterruptedException) {
        }
        NowDate = new Date();
      }

      if (!this.m_Port.isRealtimeStatusAvailable().booleanValue())
      {
        retval = DFDF1.ERROR_CODE.TIMEOUT;
      }
    }

    return retval;
  }

  public byte getDeviceStatus()
  {
    if (this.m_Port == null)
    {
      return -1;
    }

    return this.m_Port.getRealtimeStatus();
  }

  public DFDF1.ERROR_CODE activateASB(Boolean drawer, Boolean onoffline, Boolean error, Boolean paper, Boolean slip, Boolean panelbutton)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    byte commandByte = 0;

    CCCC.d("GpComDevice", "------- activateASB method -------");
    CCCC.d("GpComDevice", "m_Port==null? " + Boolean.toString(this.m_Port == null));

    if (this.m_Port == null)
    {
      return DFDF1.ERROR_CODE.FAILED;
    }

    if (drawer.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x1);
    }
    if (onoffline.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x2);
    }
    if (error.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x4);
    }
    if (paper.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x8);
    }
    if (slip.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x20);
    }
    if (panelbutton.booleanValue())
    {
      commandByte = (byte)(commandByte | 0x40);
    }

    String command = String.format("GS a %d", new Object[] { Byte.valueOf(commandByte) });
    Vector binaryData = EEE.convertEscposToBinary(command);
    CCCC.d("GpComDevice", "binaryData==null? " + Boolean.toString(binaryData == null));
    CCCC.d("GpComDevice", "binaryData.size()=" + Integer.toString(binaryData.size()));

    retval = this.m_Port.writeData(binaryData);
    CCCC.d("GpComDevice", "m_Port.writeData returned: " + retval.toString());

    return retval;
  }

//  public GpCom.ERROR_CODE readMICR(GpCom.MICR_FONT font, Boolean waitforanswer, int timeout)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    int MICRFont = 0;
//
//    if (this.m_Port == null)
//    {
//      return GpCom.ERROR_CODE.FAILED;
//    }
//
//    switch (font)
//    {
//    case CMC7:
//      MICRFont = 0;
//      break;
//    case E13B:
//      MICRFont = 1;
//      break;
//    default:
//      retval = GpCom.ERROR_CODE.INVALID_FONT;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = String.format("FS a '0' %d", new Object[] { Integer.valueOf(MICRFont) });
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if ((retval == GpCom.ERROR_CODE.SUCCESS) && (waitforanswer.booleanValue()))
//    {
//      Date NowDate = new Date();
//      Date TimeoutDate = new Date(NowDate.getTime() + timeout * 1000);
//
//      while ((!this.m_Port.isMICRStringAvailable().booleanValue()) && (NowDate.before(TimeoutDate)))
//      {
//        try
//        {
//          Thread.sleep(400L);
//        } catch (InterruptedException localInterruptedException) {
//        }
//        NowDate = new Date();
//      }
//
//      if (!this.m_Port.isMICRStringAvailable().booleanValue())
//      {
//        retval = GpCom.ERROR_CODE.TIMEOUT;
//      }
//    }
//
//    return retval;
//  }

  public ASBStatus getASB()
  {
    Vector ASBData = new Vector();
    ASBStatus retval = null;

    if (this.m_Port != null)
    {
      ASBData = this.m_Port.getASB();
      DFDF1.ERROR_CODE rv = this.m_ASBStatus.setASBStatus(ASBData);
      if (rv == DFDF1.ERROR_CODE.SUCCESS)
      {
        retval = this.m_ASBStatus;
      }
    }

    return retval;
  }

//  public String getMICRString()
//  {
//    if (this.m_Port != null)
//    {
//      return this.m_Port.getMICR();
//    }
//
//    return null;
//  }

//  public Vector<Byte> getImageData()
//  {
//    if (this.m_Port != null)
//    {
//      return this.m_Port.getImageData();
//    }
//
//    return null;
//  }

//  public byte[] getImageDataBytes()
//  {
//    if (this.m_Port != null)
//    {
//      return this.m_Port.getImageDataBytes();
//    }
//
//    return null;
//  }

//  public GpCom.ERROR_CODE setImageParameters(GpCom.BITDEPTH bitdepth, GpCom.IMAGEPROCESSING imageprocessing, int threshold)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      switch (bitdepth)
//      {
//      case BW:
//        break;
//      case GRAYSCALE:
//        break;
//      default:
//        retval = GpCom.ERROR_CODE.INVALID_BIT_DEPTH;
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      switch (imageprocessing)
//      {
//      case NONE:
//        break;
//      case SHARPENING:
//        break;
//      default:
//        retval = GpCom.ERROR_CODE.INVALID_IMAGE_PROCESSING;
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      if ((threshold < -128) || (threshold > 127))
//      {
//        retval = GpCom.ERROR_CODE.INVALID_THRESHOLD;
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      this.m_ScanParameters.BitDepth = bitdepth;
//      this.m_ScanParameters.ImageProcessing = imageprocessing;
//      this.m_ScanParameters.Threshold = threshold;
//    }
//
//    return retval;
//  }

//  public GpCom.ERROR_CODE setScanArea(int x1, int y1, int x2, int y2)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    if (this.m_Port == null)
//    {
//      retval = GpCom.ERROR_CODE.FAILED;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      if ((x1 < 0) || (x1 > 98) || (x2 < 0) || (x2 > 100) || (x2 == 1) || 
//        (y1 < 0) || (y1 > 228) || (y2 < 0) || (y2 > 230) || (y2 == 1))
//      {
//        retval = GpCom.ERROR_CODE.INVALID_SCAN_AREA;
//      }
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      if ((!GpTools.isEven(x1).booleanValue()) || (!GpTools.isEven(y1).booleanValue()) || (!GpTools.isEven(x2).booleanValue()) || (!GpTools.isEven(y2).booleanValue()))
//      {
//        retval = GpCom.ERROR_CODE.INVALID_SCAN_AREA;
//      }
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "FS ( g 2 0 32 48";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = String.format("FS ( g 5 0 41 %d %d %d %d", new Object[] { Integer.valueOf(x1), Integer.valueOf(y1), Integer.valueOf(x2), Integer.valueOf(y2) });
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    return retval;
//  }

  public DFDF1.ERROR_CODE setCropArea(int num, int x1, int y1, int x2, int y2)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if ((num < 1) || (num > 10))
      {
        retval = DFDF1.ERROR_CODE.INVALID_CROP_AREA_INDEX;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if ((x1 < 0) || (x1 > 98) || (x2 < 2) || (x2 > 100) || 
        (y1 < 0) || (y1 > 228) || (y2 < 2) || (y2 > 230))
      {
        retval = DFDF1.ERROR_CODE.INVALID_CROP_AREA;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if ((!EEE.isEven(x1).booleanValue()) || (!EEE.isEven(y1).booleanValue()) || (!EEE.isEven(x2).booleanValue()) || (!EEE.isEven(y2).booleanValue()))
      {
        retval = DFDF1.ERROR_CODE.INVALID_CROP_AREA;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "FS ( g 2 0 32 48";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = String.format("FS ( g 6 0 57 %d %d %d %d %d", new Object[] { Integer.valueOf(num), Integer.valueOf(x1), Integer.valueOf(y1), Integer.valueOf(x2), Integer.valueOf(y2) });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }
    return retval;
  }

  public DFDF1.ERROR_CODE deleteCropArea(int num)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if ((num < 1) || (num > 10))
      {
        retval = DFDF1.ERROR_CODE.INVALID_CROP_AREA_INDEX;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = String.format("FS ( g 2 0 56 %d", new Object[] { Integer.valueOf(num) });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

//  public GpCom.ERROR_CODE selectImageFormat(GpCom.IMAGEFORMAT format)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    switch (format)
//    {
//    case BMP:
//      break;
//    case JPEG_HIGH:
//      break;
//    case JPEG_LOW:
//      break;
//    case JPEG_MED:
//      break;
//    case RAW:
//      break;
//    case TIFF:
//      break;
//    case TIFF_COMP:
//      break;
//    default:
//      retval = GpCom.ERROR_CODE.INVALID_IMAGE_FORMAT;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      this.m_ScanParameters.ImageFormat = format;
//    }
//
//    return retval;
//  }

  public DFDF1.ERROR_CODE selectReceiptPaper()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "GS ( G 2 0 80 1";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE selectSlipPaper(DFDF1.PAPERSIDE side)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    String command = "";

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      switch (side)
      {
      case BACK:
        command = "GS ( G 2 0 48 4";
        break;
      case FRONT:
        command = "GS ( G 2 0 48 68";
        break;
      default:
        retval = DFDF1.ERROR_CODE.INVALID_PAPER_SIDE;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE selectAlignment(DFDF1.ALIGNMENT alignment)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    int iAlignment = 0;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      switch (alignment)
      {
      case LEFT:
        iAlignment = 0;
        break;
      case CENTER:
        iAlignment = 1;
        break;
      case RIGHT:
        iAlignment = 2;
        break;
      default:
        retval = DFDF1.ERROR_CODE.INVALID_JUSTIFICATION;
      }

    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = String.format("ESC a %d", new Object[] { Integer.valueOf(iAlignment) });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE selectPageMode()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "ESC L";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE selectPrintDirection(DFDF1.PRINTDIRECTION direction)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    int iPrintDirection = 0;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      switch (direction)
      {
      case BOTTOMTOTOP:
        iPrintDirection = 0;
        break;
      case LEFTTORIGHT:
        iPrintDirection = 1;
        break;
      case RIGHTTOLEFT:
        iPrintDirection = 2;
        break;
      case TOPTOBOTTOM:
        iPrintDirection = 3;
        break;
      default:
        retval = DFDF1.ERROR_CODE.INVALID_PRINT_DIRECTION;
      }

    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = String.format("ESC T %d", new Object[] { Integer.valueOf(iPrintDirection) });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE feedSlipToPrintStartPosition()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "GS ( G 2 0 84 1";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE ejectCheck()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "FS a '2'";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE cancelWaitForSlip()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "DLE ENQ 3";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE printString(String string, DFDF1.FONT font, Boolean bold, Boolean underlined, Boolean doubleHeight, Boolean doubleWidth)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    int options = 0;
    String command = "";

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      switch (font)
      {
      case FONT_A:
        command = "ESC M 0";
        break;
      case FONT_B:
        command = "ESC M 1";
        break;
      default:
        retval = DFDF1.ERROR_CODE.INVALID_FONT;
      }

    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if (bold.booleanValue())
      {
        command = "ESC E 1";
      }
      else
      {
        command = "ESC E 0";
      }
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      if (underlined.booleanValue())
      {
        command = "ESC - 49";
      }
      else
      {
        command = "ESC - 48";
      }
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      options = 0;

      if (doubleHeight.booleanValue())
      {
        options |= 1;
      }

      if (doubleWidth.booleanValue())
      {
        options |= 16;
      }

      command = String.format("GS ! %d", new Object[] { Integer.valueOf(options) });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      command = String.format("'%s' LF", new Object[] { string });
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }
    return retval;
  }

  public DFDF1.ERROR_CODE printPage(int mode)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
    String command = "";

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      switch (mode)
      {
      case 0:
        command = "FF";
        break;
      case 1:
        command = "ESC FF";
        break;
      default:
        retval = DFDF1.ERROR_CODE.INVALID_PRINT_PAGE_MODE;
      }
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE cutPaper()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "GS V 65 20";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

//  public GpCom.ERROR_CODE scanCheck(Boolean waitforanswer, int timeout)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    if (this.m_Port == null)
//    {
//      retval = GpCom.ERROR_CODE.FAILED;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      retval = checkAndSetImageParameters(0);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "ESC c '0' 4";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 2 0 80 32";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 5 0 65 1 0 48 48";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if ((retval == GpCom.ERROR_CODE.SUCCESS) && (waitforanswer.booleanValue()))
//    {
//      Date NowDate = new Date();
//      Date TimeoutDate = new Date(NowDate.getTime() + timeout * 1000);
//
//      while ((!this.m_Port.isImageDataAvailable().booleanValue()) && (NowDate.before(TimeoutDate)))
//      {
//        try
//        {
//          Thread.sleep(500L);
//        } catch (InterruptedException localInterruptedException) {
//        }
//        NowDate = new Date();
//      }
//
//      if (!this.m_Port.isImageDataAvailable().booleanValue())
//      {
//        retval = GpCom.ERROR_CODE.TIMEOUT;
//      }
//    }
//
//    return retval;
//  }

//  public GpCom.ERROR_CODE readMICRScanCheck(GpCom.MICR_FONT font, Boolean waitforanswer, int timeout)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    int MICRFont = 0;
//
//    if (this.m_Port == null)
//    {
//      retval = GpCom.ERROR_CODE.FAILED;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      switch (font)
//      {
//      case CMC7:
//        MICRFont = 0;
//        break;
//      case E13B:
//        MICRFont = 1;
//        break;
//      default:
//        retval = GpCom.ERROR_CODE.INVALID_FONT;
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      retval = checkAndSetImageParameters(0);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = String.format("FS a '0' %d", new Object[] { Integer.valueOf(MICRFont) });
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "FS a '1'";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "ESC c '0' 4";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 2 0 80 32";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 5 0 65 1 0 48 48";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if ((retval == GpCom.ERROR_CODE.SUCCESS) && (waitforanswer.booleanValue()))
//    {
//      Date NowDate = new Date();
//      Date TimeoutDate = new Date(NowDate.getTime() + timeout * 1000);
//
//      while (((!this.m_Port.isMICRStringAvailable().booleanValue()) || (!this.m_Port.isImageDataAvailable().booleanValue())) && (NowDate.before(TimeoutDate)))
//      {
//        try
//        {
//          Thread.sleep(500L);
//        } catch (InterruptedException localInterruptedException) {
//        }
//        NowDate = new Date();
//      }
//
//      if ((!this.m_Port.isMICRStringAvailable().booleanValue()) || (!this.m_Port.isImageDataAvailable().booleanValue()))
//      {
//        retval = GpCom.ERROR_CODE.TIMEOUT;
//      }
//    }
//
//    return retval;
//  }

//  public GpCom.ERROR_CODE scanIDCard(Boolean waitforanswer, int timeout)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//
//    if (this.m_Port == null)
//    {
//      retval = GpCom.ERROR_CODE.FAILED;
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      retval = checkAndSetImageParameters(1);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 2 0 80 16";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 2 0 83 48";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      String command = "GS ( G 5 0 65 1 0 48 48";
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    if ((retval == GpCom.ERROR_CODE.SUCCESS) && (waitforanswer.booleanValue()))
//    {
//      Date NowDate = new Date();
//      Date TimeoutDate = new Date(NowDate.getTime() + timeout * 1000);
//
//      while ((!this.m_Port.isImageDataAvailable().booleanValue()) && (NowDate.before(TimeoutDate)))
//      {
//        try
//        {
//          Thread.sleep(500L);
//        } catch (InterruptedException localInterruptedException) {
//        }
//        NowDate = new Date();
//      }
//
//      if (!this.m_Port.isImageDataAvailable().booleanValue())
//      {
//        retval = GpCom.ERROR_CODE.TIMEOUT;
//      }
//    }
//
//    return retval;
//  }

  public DFDF1.ERROR_CODE resetDevice()
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (this.m_Port == null)
    {
      retval = DFDF1.ERROR_CODE.FAILED;
    }

    if (retval == DFDF1.ERROR_CODE.SUCCESS)
    {
      String command = "ESC @";
      Vector binaryData = EEE.convertEscposToBinary(command);
      retval = this.m_Port.writeData(binaryData);
    }

    return retval;
  }

  public DFDF1.ERROR_CODE registerCallback(A2 callback)
  {
    DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

    if (callback == null)
    {
      retval = DFDF1.ERROR_CODE.INVALID_CALLBACK_OBJECT;
    }
    else
    {
      this.m_callback = callback;
    }
    return retval;
  }

  public DFDF1.ERROR_CODE unregisterCallback()
  {
    this.m_callback = null;

    return DFDF1.ERROR_CODE.SUCCESS;
  }

//  public GpCom.ERROR_CODE checkAndSetImageParameters(int checkOrCard)
//  {
//    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
//    String command = "";
//
//    int bitdepth = 0;
//    int imageprocessing = 0;
//    byte threshold = 0;
//
//    if ((this.m_ScanParameters.ImageFormat == GpCom.IMAGEFORMAT.TIFF) && (this.m_ScanParameters.BitDepth == GpCom.BITDEPTH.BW))
//    {
//      retval = GpCom.ERROR_CODE.INVALID_PARAMETER_COMBINATION;
//    }
//
//    if ((this.m_ScanParameters.ImageFormat == GpCom.IMAGEFORMAT.TIFF_COMP) && (this.m_ScanParameters.BitDepth == GpCom.BITDEPTH.GRAYSCALE))
//    {
//      retval = GpCom.ERROR_CODE.INVALID_PARAMETER_COMBINATION;
//    }
//
//    if (((this.m_ScanParameters.ImageFormat == GpCom.IMAGEFORMAT.JPEG_HIGH) || (this.m_ScanParameters.ImageFormat == GpCom.IMAGEFORMAT.JPEG_MED) || 
//      (this.m_ScanParameters.ImageFormat == GpCom.IMAGEFORMAT.JPEG_LOW)) && (this.m_ScanParameters.BitDepth == GpCom.BITDEPTH.BW))
//    {
//      retval = GpCom.ERROR_CODE.INVALID_PARAMETER_COMBINATION;
//    }
//
//    if (checkOrCard == 1)
//    {
//      if (this.m_ScanParameters.BitDepth == GpCom.BITDEPTH.BW)
//      {
//        retval = GpCom.ERROR_CODE.INVALID_PARAMETER_FOR_CARDSCAN;
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      if (checkOrCard == 0)
//      {
//        command = "FS ( g 2 0 32 48";
//        Vector binaryData = GpTools.convertEscposToBinary(command);
//        retval = this.m_Port.writeData(binaryData);
//      }
//      else
//      {
//        command = "FS ( g 2 0 32 49";
//        Vector binaryData = GpTools.convertEscposToBinary(command);
//        retval = this.m_Port.writeData(binaryData);
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      switch (this.m_ScanParameters.ImageFormat.ordinal()+1)
//      {
//      case 1:
//        command = "FS ( g 3 0 50 48 48";
//        break;
//      case 2:
//        command = "FS ( g 3 0 50 48 49";
//        break;
//      case 3:
//        command = "FS ( g 3 0 50 48 50";
//        break;
//      case 4:
//        command = "FS ( g 3 0 50 49 48";
//        break;
//      case 5:
//        command = "FS ( g 3 0 50 50 48";
//        break;
//      case 6:
//        command = "FS ( g 3 0 50 50 49";
//        break;
//      case 7:
//        command = "FS ( g 3 0 50 50 50";
//        break;
//      default:
//        command = "";
//        retval = GpCom.ERROR_CODE.INVALID_IMAGE_FORMAT;
//      }
//
//      if (retval == GpCom.ERROR_CODE.SUCCESS)
//      {
//        Vector binaryData = GpTools.convertEscposToBinary(command);
//        retval = this.m_Port.writeData(binaryData);
//      }
//
//    }
//
//    if (retval == GpCom.ERROR_CODE.SUCCESS)
//    {
//      switch (this.m_ScanParameters.BitDepth.ordinal()+1)
//      {
//      case 1:
//        bitdepth = 1;
//        break;
//      case 2:
//        bitdepth = 8;
//      }
//
//      switch (this.m_ScanParameters.ImageProcessing.ordinal()+1)
//      {
//      case 1:
//        imageprocessing = 49;
//        break;
//      case 2:
//        imageprocessing = 50;
//      }
//
//      threshold = (byte)this.m_ScanParameters.Threshold;
//
//      command = String.format("FS ( g 5 0 40 48 %d %d %d", new Object[] { Integer.valueOf(bitdepth), Integer.valueOf(imageprocessing), Byte.valueOf(threshold) });
//      Vector binaryData = GpTools.convertEscposToBinary(command);
//      retval = this.m_Port.writeData(binaryData);
//    }
//
//    return retval;
//  }
}
