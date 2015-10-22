package com.sky;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.sky.R;
import com.sky.RefreshListView.onStateChangeListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements onStateChangeListener {
	private RefreshListView refreshListView;

	private ArrayList<String> datas = new ArrayList<String>();

	private MyAdapter adapter = null;

	private ImageView iv_arrow = null;
	private ProgressBar prograssbar = null;
	private TextView iv_state = null;
	private TextView iv_time = null;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			adapter.notifyDataSetChanged();

			prograssbar.setVisibility(View.INVISIBLE);
			iv_arrow.setVisibility(View.VISIBLE);
			iv_state.setText("加载完成");
			iv_time.setText("上次刷新时间：" + getCurrentTime());
			refreshListView.freshComplete(); // 完成刷新操作后 设置显示状态 必须要执行
		};
	};

	private RotateAnimation upAnimation = null, downAnimation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
		refreshListView.setHeaderView(R.layout.layout_header);
		
		View v = refreshListView.getHeadView();
		iv_arrow = (ImageView) v.findViewById(R.id.iv_arrow);
		prograssbar = (ProgressBar) v.findViewById(R.id.pb_rotate);
		iv_state = (TextView) v.findViewById(R.id.tv_state);
		iv_time = (TextView) v.findViewById(R.id.tv_time);

		initRotateAnimation();

		for (int i = 0; i < 50; i++) {
			datas.add("数据 - " + i);
		}

		refreshListView.setOnStateChangedListener(MainActivity.this);
		adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);

	}

	private void requestDataFromServer() {
		new Thread() {
			public void run() {
				SystemClock.sleep(5000);
				for (int i = 0; i < 6; i++) {
					datas.add(i, "添加数据-->" + i);
				}
				mHandler.sendEmptyMessage(0);
			};
		}.start();
	}

	class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {// 暂时用不到的方法
			return null;
		}

		@Override
		public long getItemId(int position) {// 用不到的方法
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(MainActivity.this);
			textView.setPadding(20, 20, 20, 20);
			textView.setTextSize(18);
			textView.setText(datas.get(position));

			return textView;
		}

	}

	private void initRotateAnimation() {
		upAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(300);
		upAnimation.setFillAfter(true);
		downAnimation = new RotateAnimation(-180, -360,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(300);
		downAnimation.setFillAfter(true);
	}

	@Override
	public void onStat_prepareToRefresh() {// 满足触发条件
		iv_state.setText("松开刷新");
		iv_arrow.startAnimation(upAnimation);
	}

	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	@Override
	public void onState_REFRESHING() {// 刷新中
		iv_arrow.clearAnimation();
		iv_arrow.setVisibility(View.INVISIBLE);
		prograssbar.setVisibility(View.VISIBLE);
		iv_state.setText("正在刷新。。。。");
		requestDataFromServer();
	}

	@Override
	public void reFreshcomplete() {
		// 刷新完毕后修正显示状态
		iv_state.setText("下拉刷新");
		iv_arrow.startAnimation(downAnimation);
	}

}
