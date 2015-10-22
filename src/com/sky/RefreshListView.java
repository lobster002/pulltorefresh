package com.sky;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/*
 * 自定义控件
 * 需先设置自定义头布局 setHeadView(int id)，并通过getHeadView()获取头布局 返回View
 * 通过View 拿到自定义头布局中的子控件  PS：需要在setAdapter()之前调用
 * 
 * 调用时 需要实现 onStateChangeListener接口
 * onStat_prepareToRefresh()：下拉距离满足刷新条件是调用
 * onState_REFRESHING() ：刷新过程中调用
 * reFreshcomplete()：刷新完成后调用
 * 以上三个方法 均用于修正头布局显示内容 或者触发 刷新操作
 * */

public class RefreshListView extends ListView {

	private View v;

	private int headHeight;
	private int downY;

	private final int PULL_REFRESH = 0;
	private final int RELEASE_REFRESH = 1;
	private final int REFRESHING = 2;
	private int currentState = PULL_REFRESH;

	private onStateChangeListener listener = null;

	public interface onStateChangeListener {
		public void onStat_prepareToRefresh();// 满足触发刷新条件

		public void onState_REFRESHING();// 开始刷新

		public void reFreshcomplete();
	}

	public RefreshListView(Context context) {
		super(context);
	}

	public void setOnStateChangedListener(onStateChangeListener listener) {
		this.listener = listener;
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setHeaderView(int id) {
		v = View.inflate(getContext(), id, null);
		v.measure(0, 0);
		headHeight = v.getMeasuredHeight();
		v.setPadding(0, -headHeight, 0, 0);

		addHeaderView(v);
	}

	public View getHeadView() {
		return v;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (REFRESHING == currentState) {
				break;
			}
			int deltaY = (int) (ev.getY() - downY);
			int paddingTop = -headHeight + deltaY;
			if (paddingTop > -headHeight && getFirstVisiblePosition() == 0) {
				v.setPadding(0, paddingTop, 0, 0);
				if (paddingTop >= 0 && PULL_REFRESH == currentState) {
					currentState = RELEASE_REFRESH;
					refreshHeaderView();
				} else if (paddingTop < 0 && RELEASE_REFRESH == currentState) {
					currentState = PULL_REFRESH;
					refreshHeaderView();
				}
				return true;
			}
			break;
			
		case MotionEvent.ACTION_UP:
			if (PULL_REFRESH == currentState) {
				v.setPadding(0, -headHeight, 0, 0);
			} else if ( RELEASE_REFRESH  == currentState) {
				v.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshHeaderView();
			}
			break;
		}
		
		return super.onTouchEvent(ev);
	}

	private void refreshHeaderView() {
		switch (currentState) {
		case RELEASE_REFRESH:
			if (null != listener) {
				listener.onStat_prepareToRefresh();
			}
			break;
		case REFRESHING:
			if (null != listener) {
				listener.onState_REFRESHING();
			}
			break;
		}
	}

	public void completeRefresh() {
		v.setPadding(0, -headHeight, 0, 0);
	}

	public void freshComplete() {
		if (null != v) {
			v.setPadding(0, -headHeight, 0, 0);
		}
		currentState = PULL_REFRESH;
		if (null != listener) {
			listener.reFreshcomplete();
		}
	}

}
