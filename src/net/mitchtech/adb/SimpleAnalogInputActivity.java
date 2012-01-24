package net.mitchtech.adb;

import java.io.IOException;

import net.mitchtech.adb.simpleanaloginput.R;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Server;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

public class SimpleAnalogInputActivity extends Activity {
	private final String TAG = SimpleAnalogInputActivity.class.getSimpleName();
	
	TextView mValueTextView;
	SeekBar mValueSeekBar;

	private int mSensorValue = 10;

	// Create TCP server (based on MicroBridge LightWeight Server).
	// Note: This Server runs in a separate thread.
	Server mServer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// Create TCP server (based on MicroBridge LightWeight Server)
		try {
			mServer = new Server(4568); // Use ADK port
			mServer.start();
		} catch (IOException e) {
			Log.e(TAG, "Unable to start TCP server", e);
			System.exit(-1);
		}

		mServer.addListener(new AbstractServerListener() {

			@Override
			public void onReceive(org.microbridge.server.Client client, byte[] data) {

				if (data.length < 2)
					return;
				mSensorValue = (data[0] & 0xff) | ((data[1] & 0xff) << 8);

				// Any update to UI can not be carried out in a non UI thread
				// like the one used for Server. Hence runOnUIThread is used.
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						new UpdateData().execute(mSensorValue);
					}
				});
			}
		});

	} // End of TCP Server code

	// UpdateData Asynchronously sends the value received from ADK Main Board.
	// This is triggered by onReceive()
	class UpdateData extends AsyncTask<Integer, Integer, String> {
		// Called to initiate the background activity
		@Override
		protected String doInBackground(Integer... sensorValue) {

			// Init SeeekBar Widget to display ADC sensor value in SeekBar
			// Max value of SeekBar is set to 1024
			SeekBar sbAdcValue = (SeekBar) findViewById(R.id.sbValue);
			sbAdcValue.setProgress(sensorValue[0]);
			return (String.valueOf(sensorValue[0])); // This goes to result
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			// Init TextView Widget to display ADC sensor value in numeric.
			TextView tvAdcvalue = (TextView) findViewById(R.id.tvValue);
			tvAdcvalue.setText(String.valueOf(result));
		}
	}

}
