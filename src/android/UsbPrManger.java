package cordova.plugin.gc099printer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.usbsdk.A2;
 
import com.usbsdk.EEE11A;
import com.usbsdk.DD1;
import com.usbsdk.DFDF4;

import comon.error.DFDF1;
import comon.error.DFDF1.ERROR_CODE;

// import github.com.debug;
import hdx.HdxUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;



public class UsbPrManger implements A2 {
    private final String TAG="UsbPrManger";
    /**
     * usb打印设备
     */
    private DD1 m_Device;

    /**
     * usb打印参数
     */
    private DFDF4 m_DeviceParameters;


    private Context context;

    private String usbPrType ="USB";

    private String USB_CHARSET="cp936";

    private int operCount=5;


    /**
     *
     *
     *
     * @param context
     */

    public UsbPrManger(Context context){
         this.context = context;
        initUsbPr();

    }


    /**
     * 初始化打印
     */
    public  void initUsbPr(){

        m_Device = new DD1();
        m_DeviceParameters = new DFDF4();
        m_Device.registerCallback(this);
    }


    /**
     * 打开Usb端口
     */
    public void openUsbPrDevice()
    {
        Log.i(TAG,"openUsbPrDevice");

        DFDF1.ERROR_CODE err = DFDF1.ERROR_CODE.SUCCESS;

      

        if(usbPrType.equals("USB"))
        {

            //设置参数
            m_DeviceParameters.PortType = DFDF1.PORT_TYPE.USB;
            m_DeviceParameters.PortName = "";
            m_DeviceParameters.ApplicationContext = context;
            Log.i(TAG,"openUsbPrDevice->usbPrType->"+ DFDF1.PORT_TYPE.USB.name());
        }
        else
        {
            err = DFDF1.ERROR_CODE.INVALID_PORT_TYPE;
            Log.i(TAG,"openUsbPrDevice->INVALID_PORT_TYPE->");
        }

        if(err== DFDF1.ERROR_CODE.SUCCESS)
        {
            //set the parameters to the device
            err = m_Device.setDeviceParameters(m_DeviceParameters);
            if(err!= DFDF1.ERROR_CODE.SUCCESS)
            {
                String errorString = DFDF1.getErrorText(err);
                showMessageBox(context,errorString, "参数设置错误！");
            }

            if(err== DFDF1.ERROR_CODE.SUCCESS)
            {
                //open the device
                err = m_Device.openDevice();
                if(err!=DFDF1.ERROR_CODE.SUCCESS)
                {
                    //第二次打开设备
                    err = m_Device.openDevice();

                    if(err!=DFDF1.ERROR_CODE.SUCCESS){
                        //重复打开连接设备
                        for (int i=0;i<operCount;i++){
                            Log.i(TAG,"openUsbPrDevice->retry-->open->"+i);
                            err = m_Device.openDevice();

                            if(err==DFDF1.ERROR_CODE.SUCCESS)
                            {
                                break;
                            }else{
                                String errorString = DFDF1.getErrorText(err);
                                showMessageBox(context,errorString, "打开设备错误,请重启设备！");
                                sleep(100);
                            }

                        }
                    }

                    Log.i(TAG,"open Success");
                }
            }

            if(err== DFDF1.ERROR_CODE.SUCCESS)
            {

                err = m_Device.activateASB(true, true, true, true, true, true);

                if(err!= DFDF1.ERROR_CODE.SUCCESS)
                {
                    String errorString = DFDF1.getErrorText(err);
                     showMessageBox(context,errorString, "打开设备错误！");
                }
            }
        }



    }

    /**
     * 关闭Usb打印设备
     *
     */
    public void closeUsbPrDevice()
    {
        DFDF1.ERROR_CODE err = m_Device.closeDevice();
        if(err!= DFDF1.ERROR_CODE.SUCCESS)
        {
            String errorString = DFDF1.getErrorText(err);
            showMessageBox(context, errorString, "关闭设备错误！");
        }

   


    }


    public boolean isOpen(){
        if(null != m_Device){
            return m_Device.isDeviceOpen();
        }
        return false;
    }

 public void test2(byte [] data)
 {
	 
	 while(!m_Device.isDeviceOpen())
 	{
 		 Log.i(TAG," !m_Device.isDeviceOpen(;");
 		   openUsbPrDevice();
 		    
     		  break;
 	}
	 Log.i(TAG,"  m_Device.isDeviceOpen(;");
	
	
	 SendLongDataToUart2(data);
	  sleep(3000);
	 
	  int temp = 0;
	  releaseUsbpr();
	while(!m_Device.isDeviceOpen())
	{
		  Log.i(TAG,"end---   !m_Device.isDeviceOpen()");
	 
	   openUsbPrDevice();
	   Log.i(TAG,"end---     openUsbPrDevice();");
  		   if(temp++==15)
  		   {
	   
	  			   releaseUsbpr();
	  			   PowerOff();
	  			   PowerOn_and_Delay();
	  			   // 遇到 验证错误   ,关闭打印机连接 ,下电 , 延迟 再上电 ,再延迟 再连接 .
		 
			   
		   }
		   if(temp == 40)
		   {
			   
			   return;
		   }
	} 
 }
    /**
     * 打印XMl字符串
     * @param xml_str
     */
    public void printXMlStr(String xml_str)
    {

        if(TextUtils.isEmpty(xml_str))
            return;

        DFDF1.ERROR_CODE err = DFDF1.ERROR_CODE.SUCCESS;

        try
        {
        	PowerOn();
        	int temp=0;
       
        	while(!m_Device.isDeviceOpen())
        	{
        		 Log.i(TAG," !m_Device.isDeviceOpen(;");
        		   openUsbPrDevice();
        		    
	        		  break;
        	}
        	 Log.i(TAG,"  m_Device.isDeviceOpen(;");



            if(m_Device.isDeviceOpen()==true)
            {

                if(err== DFDF1.ERROR_CODE.SUCCESS)
                {

                    err = printXmlCommand(xml_str);

                    if(err!=DFDF1.ERROR_CODE.SUCCESS)
                    {

                        //重复尝试打印
                        for (int i=0;i<operCount;i++){

                            err = printXmlCommand(xml_str);

                            if(err==DFDF1.ERROR_CODE.SUCCESS)
                            {
                                break;
                            }else{
                                String errorString = DFDF1.getErrorText(err);
                                showMessageBox(context, errorString, "打印错误！");
                                sleep(100);
                            }

                        }


                    }
                }
            }

            else
            {
                showMessageBox(context, "打印故障、设备未打开！", "打印错误！");

            }
        }
        catch(Exception e)
        {

            showMessageBox(context, "打印异常：", e.toString() + " - " + e.getMessage());

        }
        sleep(400); //等待打印完成 再关
        releaseUsbpr();
        int temp = 0;
    	while(!m_Device.isDeviceOpen())
    	{
    		  Log.i(TAG,"end---   !m_Device.isDeviceOpen()");
    		 
    		   openUsbPrDevice();
    		   Log.i(TAG,"end---     openUsbPrDevice();");
    		   if(temp++==15)
    		   {
     
    			   releaseUsbpr();
    			   PowerOff();
    			   PowerOn_and_Delay();
    			   // 遇到 验证错误   ,关闭打印机连接 ,下电 , 延迟 再上电 ,再延迟 再连接 .
    		 
    			   
    		   }
    		   if(temp == 40)
    		   {
    			   
    			   return;
    		   }
    	}
       

    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }  
    
    byte  []datac ={(byte)0x1d,(byte)0x28,(byte)0x6b,(byte)0x9a,(byte)0x00,(byte)0x31,(byte)0x50,(byte)0x30,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0x51,(byte)0x64,(byte)0x69,(byte)0x6E,(byte)0x47,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,
			(byte)0xB4,(byte)0x38,(byte)0x52,(byte)0x16,(byte)0x55,(byte)0x73,(byte)0x63,(byte)0x15,(byte)0x15,(byte)0xA3,
			(byte)0x4B,(byte)0x00,(byte)0xC8,(byte)0xA1,(byte)0x35,(byte)0x34,(byte)0xDC,(byte)0x36,(byte)0x99,(byte)0x18,
			(byte)0x89,(byte)0x76,(byte)0xD1,(byte)0x3C,(byte)0xF5,(byte)0x70,(byte)0x89,(byte)0xA8,(byte)0x5F,(byte)0xE6,
			(byte)0x2C,(byte)0x42,(byte)0x5C,(byte)0xE6,(byte)0x5C,(byte)0x20,(byte)0x5A,(byte)0xEB,(byte)0xF2,(byte)0x1A,
			(byte)0x5D,(byte)0xE2,(byte)0x55,(byte)0x74,(byte)0x59,(byte)0xE1,(byte)0x50,(byte)0x63,(byte)0x5A,(byte)0xE4,
			(byte)0x8C,(byte)0x48,(byte)0x54,(byte)0x92,(byte)0x23,(byte)0xB9,(byte)0x2D,(byte)0xE3,(byte)0x55,(byte)0xA9,
			(byte)0x6D,(byte)0x1d,(byte)0x28,(byte)0x6b,(byte)0x9a,(byte)0x00,(byte)0x31,(byte)0x51,(byte)0x30};
    private  DFDF1.ERROR_CODE printXmlCommand(String xml_str){

        try {


            String printStr = xml_str;

            if(!TextUtils.isEmpty(printStr)){
                //解析xml
                List<Byte[]> allList = new ArrayList<Byte[]>();

                allList.add(toBytes(xml_str.getBytes()));

                if(allList!=null && allList.size()>0){

                    byte[] allStrByte =getArray(allList);
                  
                         byte dd2[]={0x0a,0x1b, 0x61, 0x1,0x0a};
               
                    allStrByte=byteMerger(allStrByte,dd2);
                    
                    allStrByte=byteMerger(allStrByte,datac);   
                    // sleep(200);
    
                    
                   // sleep(200);
                    byte dd[]={0x0a,0x1d, 0x56, 0x30,0x0a,0x0a,0x1b, 0x61, 0x00,0x0a};
               
                    allStrByte=byteMerger(allStrByte,dd);
                    allStrByte=byteMerger(allStrByte,allStrByte);
                    
                    
                    allStrByte=byteMerger(allStrByte,allStrByte);
                    sendCommand(allStrByte);

                }



            }
            Log.i(TAG,"printSuccess");
            return  DFDF1.ERROR_CODE.SUCCESS;

        }catch (Exception e){

            e.printStackTrace();

        }

        return  DFDF1.ERROR_CODE.FAILED;

    }

    /**
     * 切纸usb
     */
  

    /**
     * 切纸usb
     */
    public void white()
    {
        DFDF1.ERROR_CODE err = DFDF1.ERROR_CODE.SUCCESS;

        try
        {


            closeUsbPrDevice();



        }
        catch(Exception e)
        {
            showMessageBox(context, "走纸错误 ：", e.getMessage());
        }
    }




    /**
     * 提示
     * @param context
     * @param errorstr
     * @param content
     */
    public void showMessageBox(Context context,String errorstr,String content){
        Toast.makeText(context,errorstr+":"+content,Toast.LENGTH_SHORT).show();
    }

    /**
     * 打开电源
     */
    public void PowerOn_and_Delay()
    {

       HdxUtil.SetPrinterPower(1);
        sleep(800);
        Log.i(TAG,"printOpenPower");

    }
    
    public void PowerOn()
    {

       HdxUtil.SetPrinterPower(1);
   
        Log.i(TAG," HdxUtil.SetPrinterPower(1);");

    }
    /**
     * 关闭电源
     */
    public void PowerOff()
    {
         HdxUtil.SetPrinterPower(0);
        sleep(100);
        Log.i(TAG,"printClosePower");

    }

    /**
     * 释放关闭Usb打印
     */
    public void releaseUsbpr(){
    	
        closeUsbPrDevice();
        if(m_Device != null && m_Device.isDeviceOpen())
        {
        	
        	  m_Device.closeDevice();
        	  Log.i(TAG,"   m_Device.closeDevice();");
        }
          
        

    }

 
    /**
     * Unicode转码
     * @param s
     * @return
     */
    private String UnicodeToGBK(String s) {
        try {
            String newstring = null;
            newstring = new String(s.getBytes("GBK"), "ISO-8859-1");
            return newstring;
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }





    /**
     * 发送命令
     * @param data
     */
    public void sendCommand(byte[]  data){
       // byte[] allStrByte ;

       // sleep(200);
       // byte dd[]={0x0a,0x1d, 0x56, 0x30,0x0a};
   
     //   allStrByte=byteMerger(datad,dd);
    	//SendLongDataToUart(datad);
    //	SendLongDataToUart(datad);
    	//SendLongDataToUart(datad);
    	SendLongDataToUart(data);
/*
        try {

//            Byte[] byteList = toBytes(data);
//            Vector<Byte> vector = new Vector<Byte>(Arrays.asList(byteList));


            Vector<Byte> vectorData = new Vector<Byte>(data.length);
            for(int i=0; i<data.length; i++) {
                vectorData.add(data[i]);
            }
            Log.e("quck"," data.length ="+ data.length);
            m_Device.sendData(vectorData);


        }catch (Exception e){

            e.printStackTrace();

        }
        sleep(20);
*/
    }

	void  print(byte [] bs)
	{
		Vector<Byte> data = new Vector<Byte>(bs.length);
		for(int i=0; i<bs.length; i++) {

		data.add(bs[i]);
		}
		
		m_Device.sendData(data);
	}
	
// 
	public void SendLongDataToUart(byte[] b  ) 
	{

		 
 
		int block_size=0x1000;
	 	int i;
		int temp;
	    int delay_time =50;
	    int delay_time2=0;
	    int count=b.length;
	  
		try {
			 
				if(b.length<=block_size)
				{
					
					print(b);
					return;
				}
				
				byte[] databuf= new byte[block_size]; 
				temp= (count )/block_size;
				//handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS, 1, 0,null));
				for(i=0;i<temp;i++)
				{
					System.arraycopy(b,i*block_size,databuf,0,block_size); 

					//debug.i("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");	
 
					print(databuf);
		 
					sleep(delay_time2);
					
				}						
				databuf= new byte[(b.length &(block_size -1) )] ; 
				int dd = b.length &(block_size -1)  ;
				System.arraycopy(b,i*block_size,databuf,0,dd); 
				print(databuf); 
 

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		 
			 
			//HdxUtil.SetPrinterPower(0);	
		}
		sleep(delay_time);
	// handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS, 1, 0,null));

				  
	}	
	public void SendLongDataToUart2(byte[] b  ) 
	{

		 
 
		int block_size=4096;
	 	int i;
		int temp;
	    int delay_time =50;
	    int delay_time2=0;
	    int count=b.length;
	  
		try {
			 
				if(b.length<=block_size)
				{
					
					print(b);
					return;
				}
				
				byte[] databuf= new byte[block_size]; 
				temp= (count )/block_size;
				//handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS, 1, 0,null));
				for(i=0;i<temp;i++)
				{
					System.arraycopy(b,i*block_size,databuf,0,block_size); 

					// debug.i("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");	
 
					print(databuf);
		 
					sleep(delay_time2);
					
				}						
				databuf= new byte[(b.length &(block_size -1) )] ; 
				int dd = b.length &(block_size -1)  ;
				System.arraycopy(b,i*block_size,databuf,0,dd); 
				print(databuf); 
 

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		 
			 
			//HdxUtil.SetPrinterPower(0);	
		}
		sleep(delay_time);
	// handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS, 1, 0,null));

				  
	}	

    private Byte[]   toBytes(byte[] bytes)
    {
        Byte[] byteList =new Byte[bytes.length];
        int i;
        for(i=0;i<bytes.length;i++)
        {

            byteList[i]= bytes[i];
        }
        return    byteList;
    }

    /**
     * 程序休眠
     * @param time
     */
    public void sleep(long time){

        try {
                Thread.sleep(time);

        }catch (Exception e){

            e.printStackTrace();

        }

    }

    public byte[] getArray(List<Byte[]> ls) {

        ByteArrayOutputStream bos=null;

        try {


            if(ls!=null && ls.size()>0){

                bos = new ByteArrayOutputStream();


                for (int i=0;i<ls.size();i++){

                    Byte[] bytes =  ls.get(i);

                    byte[] strbtye =new byte[bytes.length];

                    for (int j=0;j<bytes.length;j++){

                        strbtye[j] = bytes[j];

                    }

                    bos.write(strbtye);

                }

                bos.flush();

                bos.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return bos.toByteArray();
    }




    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return "0x" + str;//0x表示十六进制
    }


	@Override
	public ERROR_CODE CallbackMethod(EEE11A arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}