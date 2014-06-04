package com.ray.water;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class WaterGearActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ImageView iv = (ImageView)findViewById(R.id.account_setup_anim);
		AnimationDrawable ad = (AnimationDrawable) iv.getDrawable();
		ad.start();
		final WaterView wv = (WaterView)findViewById(R.id.water_view);
		wv.setCurrentPercent(0.5f);
		wv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				wv.setCurrentPercent(0.9f);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
