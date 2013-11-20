package com.fourpool.glasstagram;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.lightbox.android.photoprocessing.PhotoProcessing;

public class MainActivity extends Activity {
	private static final int TAKE_PHOTO = 0;

	private ImageView imageView;
	private Button takePhotoButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.id.image);
		takePhotoButton = (Button) findViewById(R.id.choose_image_button);

		takePhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				startActivityForResult(intent, TAKE_PHOTO);
			}
		});
		//
		// Picasso.with(this)
		// .load(new File(
		// "/mnt/sdcard/DCIM/Camera/20131119_152955_448.jpg"))
		// .resize(100, 100).into(imageView);

		Bitmap bitmap = decodeSampledBitmapFromPath(
				"/mnt/sdcard/DCIM/Camera/20131119_152955_448.jpg", 100, 100);
		imageView.setImageBitmap(bitmap);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
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

				String display = "";
				for (String key : imageReturnedIntent.getExtras().keySet()) {
					display += key + ": "
							+ imageReturnedIntent.getExtras().get(key) + "\n";
				}

				Toast.makeText(this, display, Toast.LENGTH_SHORT).show();

				File file = new File(
						imageReturnedIntent.getStringExtra("picture_file_path"));

				while (!file.exists()) {
					SystemClock.sleep(500);
					Log.e("ASDF", "About to check again");
				}

				Bitmap bitmap = decodeSampledBitmapFromPath(imageReturnedIntent.getStringExtra("picture_file_path"), 100, 100);
				Bitmap bm = PhotoProcessing.filterPhoto(bitmap, 6);
				
				imageView.setImageBitmap(bm);

				// Picasso.with(this).load(bm).resize(100, 100).into(imageView);
			}
		}
	}

	public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
