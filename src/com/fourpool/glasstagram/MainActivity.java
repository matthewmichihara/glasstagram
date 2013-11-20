package com.fourpool.glasstagram;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.glass.widget.CardScrollView;
import com.lightbox.android.photoprocessing.PhotoProcessing;

public class MainActivity extends Activity {
	private static final int TAKE_PHOTO = 0;

	private ImageView imageView;
	private Button takePhotoButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		imageView = (ImageView) findViewById(R.id.image);
//		takePhotoButton = (Button) findViewById(R.id.choose_image_button);
//
//		takePhotoButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(intent, TAKE_PHOTO);
//			}
//		});
//
		Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromPath(
				"/mnt/sdcard/DCIM/Camera/20131119_152955_448.jpg", 100, 100);
		//imageView.setImageBitmap(bitmap);

		
		CardScrollView scrollView = new CardScrollView(this);
		FilterCardScrollAdapter adapter = new FilterCardScrollAdapter(bitmap, this);
		scrollView.setAdapter(adapter);
		scrollView.activate();
		setContentView(scrollView);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				File file = new File(
						imageReturnedIntent.getStringExtra("picture_file_path"));

				while (!file.exists()) {
					SystemClock.sleep(500);
					Log.e("ASDF", "About to check again");
				}

				Bitmap bitmap = BitmapUtils
						.decodeSampledBitmapFromPath(imageReturnedIntent
								.getStringExtra("picture_file_path"), 100, 100);
				Bitmap bm = PhotoProcessing.filterPhoto(bitmap, 6);

				imageView.setImageBitmap(bm);
			}
		}
	}
}
