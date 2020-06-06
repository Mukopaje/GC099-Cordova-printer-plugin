 package com.usbsdk;
 
 import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;

import comon.error.DFDF1;
 
 public class DDDD extends FFF
 {
   InetAddress m_IPAddress;
   Socket m_Socket = null;
   Thread m_Thread;
   OutputStream m_OutStream;
   InputStream m_InStream;
   Exception m_Exception = null;
   boolean m_EthernetThreadRunning = false;
   Boolean m_CloseFlag = Boolean.valueOf(false);
   Boolean m_SendFlag = Boolean.valueOf(false);
   byte[] m_SendData;
   int m_bytesAvailable = 0;
   byte[] m_receiveData;
   Vector<Byte> m_receiveBuffer;
 
   DDDD(DFDF4 parameters)
   {
     super(parameters);
     this.m_receiveBuffer = new Vector(4096, 1024);
   }
 
   DFDF1.ERROR_CODE openPort()
   {
     Log.d("EthernetPort", "------- openPort method -------");
 
     DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
     this.m_Exception = null;
 
     this.m_Thread = new Thread(new Runnable()
     {
       public void run()
       {
         Log.d("EthernetPort", "Thread started");
 
         DDDD.this.m_SendData = null;
         DDDD.this.m_receiveData = new byte[1024];
         DDDD.this.m_receiveBuffer.clear();
         DDDD.this.m_SendFlag = Boolean.valueOf(false);
         DDDD.this.m_bytesAvailable = 0;
         Log.d("EthernetPort", "Buffers cleared");
         try
         {
           Log.d("EthernetPort", "IP address: " + DDDD.this.m_deviceParameters.IPAddress);
           Log.d("EthernetPort", "Port number: " + Integer.toString(DDDD.this.m_deviceParameters.PortNumber));
           DDDD.this.m_IPAddress = Inet4Address.getByName(DDDD.this.m_deviceParameters.IPAddress);
           SocketAddress remoteAddr = new InetSocketAddress(DDDD.this.m_IPAddress, DDDD.this.m_deviceParameters.PortNumber);
           DDDD.this.m_Socket = new Socket();
           DDDD.this.m_Socket.connect(remoteAddr, 4000);
           Log.d("EthernetPort", "Socket created");
           DDDD.this.m_OutStream = DDDD.this.m_Socket.getOutputStream();
           DDDD.this.m_InStream = DDDD.this.m_Socket.getInputStream();
           Log.d("EthernetPort", "Streams created");
         }
         catch (Exception e)
         {
           Log.d("EthernetPort", "Exception occured creating sockets and streams: " + e.getMessage());
           DDDD.this.m_Exception = e;
         }
 
         if (DDDD.this.m_Exception == null)
         {
           Log.d("EthernetPort", "Starting network loop");
           DDDD.this.m_EthernetThreadRunning = false;
           DDDD.this.m_CloseFlag = Boolean.valueOf(false);
           while (!DDDD.this.m_CloseFlag.booleanValue())
           {
             try
             {
               if (DDDD.this.m_SendFlag.booleanValue())
               {
                 Log.d("EthernetPort", "Sending data: " + Integer.toString(DDDD.this.m_SendData.length) + " bytes");
                 DDDD.this.m_OutStream.write(DDDD.this.m_SendData);
                 DDDD.this.m_OutStream.flush();
                 DDDD.this.m_SendFlag = Boolean.valueOf(false);
                 Log.d("EthernetPort", "Finished sending data");
               }
 
               DDDD.this.m_bytesAvailable = DDDD.this.m_InStream.available();
               if (DDDD.this.m_bytesAvailable > 0)
               {
                 Log.d("EthernetPort", "Receiving data: " + Integer.toString(DDDD.this.m_bytesAvailable) + " bytes");
                 int n = DDDD.this.m_InStream.read(DDDD.this.m_receiveData);
 
                 for (int i = 0; i < n; i++)
                 {
                   DDDD.this.m_receiveBuffer.add(Byte.valueOf(DDDD.this.m_receiveData[i]));
                 }
 
                 DDDD.this.saveData(DDDD.this.m_receiveBuffer);
 
                 if (DDDD.this.m_callbackInfo.ReceivedDataType != DFDF1.DATA_TYPE.NOTHING)
                 {
                   if (DDDD.this.m_callback != null)
                   {
                     DDDD.this.m_callback.CallbackMethod(DDDD.this.m_callbackInfo);
                   }
 
                   DDDD.this.afterCallbackAction(DDDD.this.m_callbackInfo.ReceivedDataType);
                 }
 
               }
               else
               {
                 Thread.sleep(50L);
               }
 
               DDDD.this.m_EthernetThreadRunning = true;
             }
             catch (Exception e)
             {
               Log.d("EthernetPort", "Exception occured in run loop: " + e.getMessage());
 
               DDDD.this.m_Exception = e;
               DDDD.this.m_CloseFlag = Boolean.valueOf(true);
             }
 
           }
 
           Log.d("EthernetPort", "Closing network");
           try
           {
             DDDD.this.m_OutStream = null;
             DDDD.this.m_Socket.close();
             DDDD.this.m_Socket = null;
           }
           catch (Exception localException1)
           {
           }
 
           DDDD.this.m_EthernetThreadRunning = false;
         }
       }
     });
     this.m_Thread.start();
     try
     {
       Thread.sleep(50L);
     }
     catch (Exception localException)
     {
     }
     while ((!this.m_EthernetThreadRunning) && (this.m_Exception == null))
     {
       try
       {
         Thread.sleep(50L);
       }
       catch (Exception localException1)
       {
       }
     }
     if (this.m_Exception != null)
     {
       if ((this.m_Exception instanceof UnknownHostException))
       {
         retval = DFDF1.ERROR_CODE.INVALID_IP_ADDRESS;
       }
       else if ((this.m_Exception instanceof SocketTimeoutException))
       {
         retval = DFDF1.ERROR_CODE.TIMEOUT;
       }
       else
       {
         retval = DFDF1.ERROR_CODE.FAILED;
       }
     }
 
     return retval;
   }
 
   DFDF1.ERROR_CODE closePort()
   {
     DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
 
     Date NowDate = new Date();
     Date TimeoutDate = new Date(NowDate.getTime() + 2000L);
 
     while (((this.m_SendFlag.booleanValue()) || (this.m_bytesAvailable > 0)) && (NowDate.before(TimeoutDate)))
     {
       try
       {
         Thread.sleep(50L);
       } catch (Exception localException) {
       }
       NowDate = new Date();
     }
 
     if (NowDate.before(TimeoutDate))
     {
       try
       {
         this.m_OutStream.flush();
         this.m_Socket.close();
         this.m_CloseFlag = Boolean.valueOf(true);
       }
       catch (IOException e)
       {
         retval = DFDF1.ERROR_CODE.FAILED;
       }
     }
     else
     {
       retval = DFDF1.ERROR_CODE.TIMEOUT;
     }
 
     return retval;
   }
 
   boolean isPortOpen()
   {
     boolean retval = true;
 
     if (this.m_Socket != null)
     {
       if ((!this.m_Socket.isConnected()) || (!this.m_Socket.isBound()) || (this.m_Socket.isClosed()) || (this.m_Socket.isInputShutdown()) || (this.m_Socket.isOutputShutdown()))
       {
         retval = false;
       }
     }
     else
     {
       retval = false;
     }
 
     return retval;
   }
   DFDF1.ERROR_CODE writeData(byte data[])
   {
     return null;
   }
   DFDF1.ERROR_CODE writeData(Vector<Byte> data)
   {
     DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
 
     if ((data != null) && (data.size() > 0))
     {
       parseOutgoingData(data);
 
       if ((this.m_Socket != null) && (this.m_OutStream != null))
       {
         Date NowDate = new Date();
         Date TimeoutDate = new Date(NowDate.getTime() + 3000L);
 
         while ((this.m_SendFlag.booleanValue()) && (NowDate.before(TimeoutDate)))
         {
           try
           {
             Thread.sleep(50L);
           } catch (InterruptedException localInterruptedException) {
           }
           NowDate = new Date();
         }
 
         if (NowDate.before(TimeoutDate))
         {
           this.m_SendData = new byte[data.size()];
 
           if (data.size() > 0)
           {
             for (int i = 0; i < data.size(); i++)
             {
               this.m_SendData[i] = ((Byte)data.get(i)).byteValue();
             }
             this.m_SendFlag = Boolean.valueOf(true);
           }
         }
         else
         {
           retval = DFDF1.ERROR_CODE.TIMEOUT;
         }
       }
       else
       {
         retval = DFDF1.ERROR_CODE.FAILED;
       }
     }
 
     return retval;
   }
 
   protected DFDF1.ERROR_CODE writeDataImmediately(Vector<Byte> data)
   {
     DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;
 
     if ((data != null) && (data.size() > 0))
     {
       byte[] sendData = new byte[data.size()];
 
       if (data.size() > 0)
       {
         for (int i = 0; i < data.size(); i++)
         {
           sendData[i] = ((Byte)data.get(i)).byteValue();
         }
 
         try
         {
           this.m_OutStream.write(sendData);
           this.m_OutStream.flush();
         }
         catch (Exception e)
         {
           Log.d("EthernetPort", "Exception occured while sending data immediately: " + e.getMessage());
           retval = DFDF1.ERROR_CODE.FAILED;
         }
       }
     }
 
     return retval;
   }
 }
