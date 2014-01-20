package tw.edu.ntu.csie.sed.CEM.activity;

import java.util.List;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.fragment.SimpleMapFragment;
import tw.edu.ntu.csie.sed.CEM.fragment.SplashFragment;
import tw.edu.ntu.csie.sed.CEM.rest.APIHelper;
import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class MainActivity extends SherlockFragmentActivity implements SimpleMapFragment.OnMapUpdatedListener,
		HTTPResponseReceiver {
	private static final int SPLASH = 0;
	private static final int MAP = 1;
	private static final int FRAGMENT_NUM = MAP + 1;

	int count = 0;

	// UI elements
	private ActionBar mActionBar;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private SimpleMapFragment mMapFragment;
	private Fragment[] mFragments = new Fragment[FRAGMENT_NUM];

	// UI controllers
	private boolean isEmotionMap = true;

	private APIHelper mAPIHelper;

	// Facebook management
	private boolean isResumed = false;
	private UiLifecycleHelper mUiHelper;
	private Session.StatusCallback mSessionStatusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUiHelper = new UiLifecycleHelper(this, mSessionStatusCallback);
		mUiHelper.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mActionBar = getSupportActionBar();
		View customActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.black_transparent));
		mActionBar.setCustomView(customActionBarView);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		FragmentManager fm = getSupportFragmentManager();
		mFragments[0] = (SplashFragment) fm.findFragmentById(R.id.fragment_splash);
		mFragments[1] = mMapFragment = (SimpleMapFragment) fm.findFragmentById(R.id.fragment_map);
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < mFragments.length; i++) {
			transaction.hide(mFragments[i]);
		}
		transaction.commit();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mAPIHelper = new APIHelper(this, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mUiHelper.onResume();
		isResumed = true;
		fetchUserList(Session.getActiveSession());
	}

	@Override
	public void onPause() {
		super.onPause();
		mUiHelper.onPause();
		isResumed = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mUiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			showFragment(MAP, false);
		} else {
			showFragment(SPLASH, false);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuitem_map).setIcon(
				isEmotionMap ? R.drawable.ic_menu_map : R.drawable.ic_menu_mission);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		menu.findItem(R.id.menuitem_map).setIcon(
				isEmotionMap ? R.drawable.ic_menu_map : R.drawable.ic_menu_mission);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}
		switch (item.getItemId()) {
		case R.id.menuitem_map:
			changeDrawer();
			break;
		case R.id.menuitem_checkin: {
			Intent intent = new Intent(MainActivity.this, CheckinActivity.class);
			startActivityForResult(intent, 0);
			break;
		}
		case R.id.menuitem_profile: {
			Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			int backStackSize = manager.getBackStackEntryCount();
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			if (state.equals(SessionState.OPENED)) {
				showFragment(MAP, false);
				fetchUserInfo(session);
			} else if (state.isClosed()) {
				showFragment(SPLASH, false);
			}
		}
	}

	private void fetchUserInfo(final Session session) {
		// String query = "SELECT uid, name, is_app_user FROM user "
		// +
		// "WHERE uid IN (SELECT uid2 FROM friend WHERE uid1=me()) AND is_app_user=1";
		//
		// Bundle params = new Bundle();
		// params.putString("q", query);
		// Request request = new Request(session, "/fql", params,
		// HttpMethod.GET, new Request.Callback() {
		// public void onCompleted(Response response) {
		// try {
		// editor.putString("friend_list",
		// response.getGraphObject().getInnerJSONObject()
		// .getJSONArray("data").toString());
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// editor.commit();
		// }
		// });
		// Request.executeBatchAsync(request);

		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (session == Session.getActiveSession()) {
					if (user != null) {
						SharedPreferences prefs = getSharedPreferences("CEM", 0);
						if (!prefs.getString("fb_id", "").equals(user.getId())) {
							mAPIHelper.login(user.getId(), user.getName());
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString("fb_id", user.getId());
							editor.putString("fb_name", user.getName());
							editor.commit();
						}
					}
				}
				if (response.getError() != null) {
					// TODO
				}
			}
		});
		request.executeAsync();
	}

	private void fetchUserList(final Session session) {

		Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() {
			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				String friendIds = "";
				for (GraphUser user : users) {
					friendIds += ";" + user.getId();
				}
				Log.i(getClass().getName(), "friend_list: " + friendIds);
				SharedPreferences prefs = getSharedPreferences("CEM", 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("friend_list", friendIds);
				editor.commit();
			}
		});
		request.executeAsync();
	}

	private void changeDrawer() {
		isEmotionMap = !isEmotionMap;
		mDrawerLayout.closeDrawer(mDrawerList);
		mMapFragment.setEmotionMapEnabled(isEmotionMap);
		invalidateOptionsMenu();
	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < mFragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(mFragments[i]);
			} else {
				transaction.hide(mFragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}

	// OnMapUpdatedListener
	@Override
	public void beforeDrawerChanged(BaseAdapter adapter) {
		mDrawerList.setAdapter(adapter);
	}

	// Implement HTTPResponseReceiver
	@Override
	public void onSuccess(int api, String result) {
		// Log.i(getClass().getName(), result);
	}

	@Override
	public void onError(int resultCode) {
		// TODO Auto-generated method stub
	}
}