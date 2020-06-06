package com.usbsdk;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

 

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import comon.error.DFDF1;

public class GGG extends FFF {
	protected static final String TAG = "USBPort";
	UsbManager m_usbManager;
	HashMap<String, UsbDevice> m_usbDeviceList;
	UsbDevice m_USBDevice = null;

	UsbInterface m_USBInterface;

	UsbEndpoint m_sendEndpoint;

	UsbEndpoint m_receiveEndpoint;

	UsbDeviceConnection m_connection;

	Context m_context;

	Thread m_Thread;

	Exception m_Exception = null;
	boolean m_USBThreadRunning = false;
	Boolean m_CloseFlag = Boolean.valueOf(false);
	Boolean m_SendFlag = Boolean.valueOf(false);
	byte[] m_SendData;
	int m_bytesAvailable = 0;

	byte[] m_receiveData = new byte[1024];

	Vector<Byte> m_receiveBuffer = new Vector<Byte>(4096, 1024);

	static UsbDevice getUsbDevice(UsbManager um) {
		HashMap<String, UsbDevice> lst = um.getDeviceList();

		Iterator<UsbDevice> deviceIterator = lst.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice dev = (UsbDevice) deviceIterator.next();
			
			CCCC.d(TAG, "usb device : " + String.format("%1$04X:%2$04X", dev.getVendorId(), dev.getProductId()));
			
			Log.d("quck","quck");
			if (dev.getVendorId() == 0x0485 ) {
				 
				return dev;
			}
			if (dev.getVendorId() == 0xB000 ) {
				 
				return dev;
			}	
			
			if (dev.getVendorId() == 0x28E9 ) {
				 
				return dev;
			}			
			 
			
			if (dev.getVendorId() == 0x0289 ) {
				 
				return dev;
			}		
			if (dev.getVendorId() == 0x28e9 ) {
				//debug.d("dev.getVendorId() == 0x28e9   :"+dev.getInterfaceCount());
				return dev;	
			}			
		}
		
		return null;
	}

	GGG(DFDF4 parameters) {
		super(parameters);
		// this.m_receiveBuffer = new Vector<Byte>(4096, 1024);
		// this.m_receiveData = new byte[1024];

		this.m_context = this.m_deviceParameters.ApplicationContext;

		CCCC.d(TAG, "Creating UsbManager...");
		this.m_usbManager = ((UsbManager) this.m_context
				.getSystemService("usb"));
		CCCC.d(TAG, "Done creating UsbManager.");
	}

	DFDF1.ERROR_CODE openPort() {
		CCCC.d(TAG, "openPort()");

		DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

		this.m_USBDevice = null;
		this.m_SendData = null;
		//this.m_receiveData = new byte[1024];
		this.m_receiveBuffer.clear();
		this.m_SendFlag = Boolean.valueOf(false);
		this.m_bytesAvailable = 0;
		CCCC.d(TAG, "Buffers cleared");

		CCCC.d(TAG, "PortName='" + this.m_deviceParameters.PortName + "'");

		this.m_usbDeviceList = this.m_usbManager.getDeviceList();
		if (!this.m_deviceParameters.PortName.equals("")) {
			CCCC.d(TAG, "PortName not empty. Trying to open it...");
			this.m_USBDevice = ((UsbDevice) this.m_usbDeviceList
					.get(this.m_deviceParameters.PortName));
		} else {
			CCCC.d(TAG, "PortName is empty. Trying to find Gp device...");

			this.m_USBDevice = getUsbDevice(this.m_usbManager);
	
			if (this.m_USBDevice != null) {
				// get permisstion
				if(!getUsbPermission())
				{
					CCCC.e(TAG, "fail to get permisstion " );
					return DFDF1.ERROR_CODE.ERROR_OR_NO_ACCESS_PERMISSION;
					
				}
					
	
				retval = connectToDevice(this.m_USBDevice);
				String errorString = DFDF1.getErrorText(retval);
				CCCC.d(TAG, "connectToDevice returned " + errorString);
			} else {
				retval = DFDF1.ERROR_CODE.NO_USB_DEVICE_FOUND;
				CCCC.d(TAG, "No device selected or found");
			}
		}
		
		return retval;
	}

	class DD13 implements Runnable {
		@Override
		public void run() {
			int sended_len=0;
			CCCC.d(TAG, "Thread started");
			CCCC.d(TAG, "m_USBDevice==null? " + Boolean.toString(GGG.this.m_USBDevice == null));

			try {
				GGG.this.m_USBInterface = GGG.this.m_USBDevice
						.getInterface(0);
				CCCC.d(TAG,
						"m_USBInterface==null? "
								+ Boolean
										.toString(GGG.this.m_USBInterface == null));
				int epCount = GGG.this.m_USBInterface.getEndpointCount();
				CCCC.d(TAG, "epCount=" + Integer.toString(epCount));

				String messageString = Integer.toString(epCount)
						+ " endpoints: ";
				for (int i = 0; i < epCount; i++) {
					messageString = messageString + Integer.toString(i) + "-";
					if (GGG.this.m_USBInterface.getEndpoint(i)
							.getDirection() == 0) {
						messageString = messageString + "out";
						GGG.this.m_sendEndpoint = GGG.this.m_USBInterface
								.getEndpoint(i);
						CCCC.d(TAG,
								"m_sendEndpoint==null? "
										+ Boolean
												.toString(GGG.this.m_sendEndpoint == null));
					} else {
						messageString = messageString + "in";
						GGG.this.m_receiveEndpoint = GGG.this.m_USBInterface
								.getEndpoint(i);
						CCCC.d(TAG,
								"m_receiveEndpoint==null? "
										+ Boolean
												.toString(GGG.this.m_receiveEndpoint == null));
					}
					messageString = messageString + " ";
				}
				CCCC.d(TAG, messageString);

				GGG.this.m_connection = GGG.this.m_usbManager
						.openDevice(GGG.this.m_USBDevice);
				CCCC.d(TAG,
						"m_connection==null? "
								+ Boolean
										.toString(GGG.this.m_connection == null));
				if (GGG.this.m_connection == null) {
					CCCC.d(TAG,
							"Error or no permission to access the port");
					GGG.this.m_Error = DFDF1.ERROR_CODE.ERROR_OR_NO_ACCESS_PERMISSION;
				} else {
					GGG.this.m_connection.claimInterface(
							GGG.this.m_USBInterface, true);
				}
			} catch (SecurityException e) {
				GGG.this.m_Exception = e;
				CCCC.d(TAG,
						"Exception in connectToDevice: " + e.toString());
				GGG.this.m_Error = DFDF1.ERROR_CODE.NO_ACCESS_GRANTED_BY_USER;
			} catch (Exception e) {
				GGG.this.m_Exception = e;
				CCCC.d(TAG,
						"Exception in connectToDevice: " + e.toString());
				GGG.this.m_Error = DFDF1.ERROR_CODE.FAILED;
			}

			if ((GGG.this.m_Exception == null)
					&& (GGG.this.m_Error == DFDF1.ERROR_CODE.SUCCESS)) {
				CCCC.d(TAG, "Starting communication loop");
				GGG.this.m_USBThreadRunning = false;
				GGG.this.m_CloseFlag = Boolean.valueOf(false);
				while (!GGG.this.m_CloseFlag.booleanValue()) {
					try {
						if (GGG.this.m_SendFlag.booleanValue()) {
							try {
								CCCC.d(TAG,"Sending data: "+ Integer.toString(GGG.this.m_SendData.length));
								sended_len=GGG.this.m_connection.bulkTransfer(
										GGG.this.m_sendEndpoint,
										GGG.this.m_SendData,
										GGG.this.m_SendData.length, 1000);

								GGG.this.m_SendFlag = Boolean
										.valueOf(false);
							} catch (Exception e) {
								CCCC.e(TAG,
										"Exception occured in send data part of run loop: "
												+ e.toString() + " - "
												+ e.getMessage());
							}
							CCCC.d(TAG,"Sended data: "+ Integer.toString(sended_len));
							if(sended_len<=0 || (sended_len!=GGG.this.m_SendData.length))
							{
								CCCC.e(TAG,"Sended err: "+ Integer.toString(sended_len));
								break;
							}
						}
	
					
						
						GGG.this.m_receiveData[0] = 0;//这是 1024字节的全局缓冲  
						int receiveCount = GGG.this.m_connection.bulkTransfer(GGG.this.m_receiveEndpoint,GGG.this.m_receiveData,
										GGG.this.m_receiveData.length,
										200);
						if (receiveCount > 0) {
							 
							m_callbackInfo.receiveCount=receiveCount;
							m_callbackInfo.m_receiveData=m_receiveData;
							if (GGG.this.m_callbackInfo.ReceivedDataType != DFDF1.DATA_TYPE.NOTHING) {
								if (GGG.this.m_callback != null) {
									GGG.this.m_callback
											.CallbackMethod(GGG.this.m_callbackInfo);
								}

							 
							}
						}	
						GGG.this.m_USBThreadRunning = true;

						Thread.sleep(30L);
					} catch (Exception e) {
						CCCC.d(TAG,
								"Exception occured in run loop: "
										+ e.getMessage());

						GGG.this.m_Exception = e;
						GGG.this.m_CloseFlag = Boolean.valueOf(true);
						GGG.this.m_Error = DFDF1.ERROR_CODE.FAILED;
					}
				}

				CCCC.d(TAG, "Closing USB port");

				try {
					GGG.this.m_connection
							.releaseInterface(GGG.this.m_USBInterface);
					GGG.this.m_connection.close();
					GGG.this.m_connection = null;
				} catch (Exception localException1) {
				}

				GGG.this.m_USBThreadRunning = false;
			}
		}	
	}

	static String ACTION_USB_PERMISSION = "com.usbsdk.USBPort.USB_PERMISSION";

	// private final BroadcastReceiver m_UsbReceiver = new BroadcastReceiver() {
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// debug.d(TAG, action);
	//
	// if (ACTION_USB_PERMISSION.equals(action)) {
	// synchronized (this) {
	// m_USBDevice =
	// (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	// /*
	// //����Ȩ������
	// if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	// // if(m_USBDevice != null){
	// // //call method to set up device communication
	// // }
	//
	// } else {
	// debug.d(TAG, "permission denied for device " + m_USBDevice);
	// }
	// */
	// context.unregisterReceiver(this);
	// }
	// }
	// }
	// };

	public static DFDF1.ERROR_CODE requestPermission(Context context) {
		UsbManager um = ((UsbManager) context.getSystemService("usb"));

		UsbDevice usbdev = getUsbDevice(um);

		if (usbdev != null) {

			// get requestPermission
			if (!um.hasPermission(usbdev)) {
				postRequestPermission(context, um, usbdev);

				return DFDF1.ERROR_CODE.ERROR_OR_NO_ACCESS_PERMISSION;
			}

			return DFDF1.ERROR_CODE.SUCCESS;
		}

		return DFDF1.ERROR_CODE.NO_USB_DEVICE_FOUND;
	}

	private static void postRequestPermission(Context context, UsbManager um,
			UsbDevice ud) {
		final BroadcastReceiver receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				CCCC.d(TAG, intent.getAction());
				context.unregisterReceiver(this);
			}
		};

		IntentFilter ifilter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(receiver, ifilter);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		um.requestPermission(ud, pi);
	}

	private boolean getUsbPermission() {
		if (m_usbManager.hasPermission(m_USBDevice)) {
			return true;
		} else {
			CCCC.d(TAG, "permission denied for device " + m_USBDevice);
			postRequestPermission(m_context, m_usbManager, m_USBDevice);
			m_USBDevice = null;
			return false;
		}
	}

	private DFDF1.ERROR_CODE connectToDevice(UsbDevice device) {
		this.m_Exception = null;
		this.m_Error = DFDF1.ERROR_CODE.SUCCESS;

		CCCC.d(TAG, "connectToDevice()");

		this.m_Thread = new Thread(new DD13());
		this.m_Thread.start();
		try {
			Thread.sleep(50L);
		} catch (Exception localException) {
		}

		while ((!this.m_USBThreadRunning) && (this.m_Exception == null)
				&& (this.m_Error == DFDF1.ERROR_CODE.SUCCESS)) {
			try {
				Thread.sleep(50L);
			} catch (Exception localException1) {
			}
		}

		if (this.m_USBThreadRunning) {
			String command = String.format("GS a 0", new Object[0]);
			Vector<Byte> binaryData = EEE.convertEscposToBinary(command);
			writeData(binaryData);
		}

		return this.m_Error;
	}

	DFDF1.ERROR_CODE closePort() {
		DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

		CCCC.d(TAG, "closePort()");

		Date NowDate = new Date();
		Date TimeoutDate = new Date(NowDate.getTime() + 2000L);

		while (((this.m_SendFlag.booleanValue()) || (this.m_bytesAvailable > 0))
				&& (NowDate.before(TimeoutDate))) {
			try {
				Thread.sleep(50L);
			} catch (Exception localException1) {
			}
			NowDate = new Date();
		}

		if (NowDate.before(TimeoutDate)) {
			try {
				this.m_connection.releaseInterface(this.m_USBInterface);
				this.m_connection.close();
				this.m_connection = null;
				this.m_CloseFlag = Boolean.valueOf(true);
			} catch (Exception e) {
				retval = DFDF1.ERROR_CODE.FAILED;
			}

		} else {
			retval = DFDF1.ERROR_CODE.TIMEOUT;
		}

		return retval;
	}

	boolean isPortOpen() {
		boolean retval = true;

		if ((this.m_connection != null) && (this.m_sendEndpoint != null)
		// fix : ���ִ�ӡ����״̬���������ܽ���ASB
		// && (this.m_receiveEndpoint != null)) {
		) {
			retval = true;
		} else {
			retval = false;
		}

		return retval;
	}

	DFDF1.ERROR_CODE writeData(Vector<Byte> data) {
		DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

		if ((data != null) && (data.size() > 0)) {
			parseOutgoingData(data);

			StringBuilder str = new StringBuilder();
			StringBuilder strBuild = new StringBuilder();
	 

	 
			if ((this.m_connection != null) && (this.m_sendEndpoint != null)) {
				Date NowDate = new Date();
				Date TimeoutDate = new Date(NowDate.getTime() + 3000L);

				while ((this.m_SendFlag.booleanValue())
						&& (NowDate.before(TimeoutDate))) {
					try {
						Thread.sleep(50L);
					} catch (InterruptedException localInterruptedException) {
					}
					NowDate = new Date();
				}

				if (NowDate.before(TimeoutDate)) {
					this.m_SendData = new byte[data.size()];

					if (data.size() > 0) {
						for (int i = 0; i < data.size(); i++) {
							this.m_SendData[i] = ((Byte) data.get(i))
									.byteValue();
						}
						this.m_SendFlag = Boolean.valueOf(true);
					}
				} else {
					retval = DFDF1.ERROR_CODE.TIMEOUT;
				}
			} else {
				retval = DFDF1.ERROR_CODE.FAILED;
			}
		}

		return retval;
	}
	
	 
	protected DFDF1.ERROR_CODE writeDataImmediately(Vector<Byte> data) {
		DFDF1.ERROR_CODE retval = DFDF1.ERROR_CODE.SUCCESS;

		if ((data != null) && (data.size() > 0)) {
			byte[] sendData = new byte[data.size()];

			if (data.size() > 0) {
				for (int i = 0; i < data.size(); i++) {
					sendData[i] = ((Byte) data.get(i)).byteValue();
				}

				try {
					this.m_connection.bulkTransfer(this.m_sendEndpoint,
							sendData, sendData.length, 100);
				} catch (Exception e) {
					CCCC.d(TAG,
							"Exception occured while sending data immediately: "
									+ e.getMessage());
					retval = DFDF1.ERROR_CODE.FAILED;
				}
			}
		}

		return retval;
	}
}
