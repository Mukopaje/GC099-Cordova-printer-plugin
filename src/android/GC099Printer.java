package cordova.plugin.gc099printer;

import java.io.UnsupportedEncodingException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;
import android.UsbPrinterManager;

/**
 * This class echoes a string called from JavaScript.
 */
public class GC099Printer extends CordovaPlugin {

    UsbPrinterManager usbPrManager;
    public String ReceiptContent;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("printString")) {
            ReceiptContent = data.getString(0);
			 new PrintReceipt().execute();
            return true;
        }
        // } else if (action.equals("printBarCode")) {
        // 	BarcodeData = data.getString(0);
		// 	 new PrintBarcode().execute();
        // return true;
        // } else if (action.equals("printQRCode")) {
        // 	QRCodeData = data.getString(0);
		// 	 new PrintQRCode().execute();
        // return true;
        // }
        return false;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
		tContext = webView.getContext();
        context = this.cordova.getActivity().getApplicationContext();

        usbPrManger = new UsbPrinterManger(context);
        	
    }

    public class PrintReceipt extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute(){			
				Toast.makeText(webView.getContext(), "Printing Please Wait... ", Toast.LENGTH_LONG).show();
		};
		
		@Override
		protected String doInBackground(Void... params){
			byte[] printContent1 = null;
			String s1 = null;
			try {
				printContent1 = strToByteArray(ReceiptContent,"UTF-8");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}
			
			try {			
                System.out.println("content print started");
                usbPrManger.test2(printContent1);
				System.out.println("content print finished");						
			}catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "Exception error ="+e);
				s1 = e.toString();
				}
			
			return s1;	
		}
	}
}
