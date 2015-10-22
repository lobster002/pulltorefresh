package com.sky;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/*
 * �Զ���ؼ�
 * ���������Զ���ͷ���� setHeadView(int id)����ͨ��getHeadView()��ȡͷ���� ����View
 * ͨ��View �õ��Զ���ͷ�����е��ӿؼ�  PS����Ҫ��setAdapter()֮ǰ����
 * 
 * ����ʱ ��Ҫʵ�� onStateChangeListener�ӿ�
 * onStat_prepareToRefresh()��������������ˢ�������ǵ���
 * onState_REFRESHING() ��ˢ�¹����е���
 * reFreshcomplete()��ˢ����ɺ����
 * ������������ ����������ͷ������ʾ���� ���ߴ��� ˢ�²���
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
		public void onStat_prepareToRefresh();// ���㴥��ˢ������

		public void onState_REFRESHING();// ��ʼˢ��

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
