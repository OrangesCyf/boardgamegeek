package com.boardgamegeek.ui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.boardgamegeek.R;
import com.boardgamegeek.io.Adapter;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.model.SearchResponse;
import com.boardgamegeek.model.SearchResult;
import com.boardgamegeek.ui.widget.BggLoader;
import com.boardgamegeek.ui.widget.Data;
import com.boardgamegeek.util.ActivityUtils;
import com.boardgamegeek.util.PreferencesUtils;
import com.boardgamegeek.util.UIUtils;
import com.boardgamegeek.util.actionmodecompat.ActionMode;
import com.boardgamegeek.util.actionmodecompat.MultiChoiceModeListener;

public class SearchResultsFragment extends BggListFragment implements
	LoaderManager.LoaderCallbacks<SearchResultsFragment.SearchData>, MultiChoiceModeListener {
	private static final int LOADER_ID = 0;

	private String mSearchText;
	private SearchResultsAdapter mAdapter;
	private LinkedHashSet<Integer> mSelectedPositions = new LinkedHashSet<Integer>();
	private MenuItem mLogPlayMenuItem;
	private MenuItem mLogPlayQuickMenuItem;
	private MenuItem mBggLinkMenuItem;

	public interface Callbacks {
		public void onResultCount(int count);

		public void onExactMatch();
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onResultCount(int count) {
		}

		@Override
		public void onExactMatch() {
		}
	};

	private Callbacks mCallbacks = sDummyCallbacks;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = UIUtils.fragmentArgumentsToIntent(getArguments());
		mSearchText = intent.getStringExtra(SearchManager.QUERY);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.empty_search));
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		ActionMode.setMultiChoiceMode(getListView(), getActivity(), this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof Callbacks)) {
			throw new ClassCastException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public Loader<SearchData> onCreateLoader(int id, Bundle data) {
		return new SearchLoader(getActivity(), mSearchText);
	}

	@Override
	public void onLoadFinished(Loader<SearchData> loader, SearchData data) {
		if (getActivity() == null) {
			return;
		}

		if (data != null && data.count() == 1 && PreferencesUtils.getSkipResults(getActivity())) {
			SearchResult game = data.list().get(0);
			ActivityUtils.launchGame(getActivity(), game.id, game.name);
			mCallbacks.onExactMatch();
			return;
		}

		if (mAdapter == null) {
			mAdapter = new SearchResultsAdapter(getActivity(), data.list());
			setListAdapter(mAdapter);
		}
		mAdapter.notifyDataSetChanged();

		if (data.hasError()) {
			setEmptyText(data.getErrorMessage());
		} else {
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
			restoreScrollState();
			mCallbacks.onResultCount(data.count());
		}
	}

	@Override
	public void onLoaderReset(Loader<SearchData> results) {
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		SearchResult game = (SearchResult) mAdapter.getItem(position);
		ActivityUtils.launchGame(getActivity(), game.id, game.name);
	}

	private static class SearchLoader extends BggLoader<SearchData> {
		private BggService mService;
		private String mQuery;

		public SearchLoader(Context context, String query) {
			super(context);
			mService = Adapter.create();
			mQuery = query;
		}

		@Override
		public SearchData loadInBackground() {
			SearchData games = null;
			try {
				if (PreferencesUtils.getExactSearch(getContext())) {
					games = new SearchData(mService.search(mQuery, BggService.SEARCH_TYPE_BOARD_GAME, 1));
				}
			} catch (Exception e) {
				// we'll try it again below
			}
			try {
				if (games == null || games.count() == 0) {
					games = new SearchData(mService.search(mQuery, BggService.SEARCH_TYPE_BOARD_GAME, 0));
				}
			} catch (Exception e) {
				games = new SearchData(e);
			}
			return games;
		}
	}

	static class SearchData extends Data<SearchResult> {
		private SearchResponse mResponse;

		public SearchData(SearchResponse response) {
			mResponse = response;
		}

		public SearchData(Exception e) {
			super(e);
		}

		public int count() {
			if (mResponse == null) {
				return 0;
			}
			return mResponse.total;
		}

		@Override
		public List<SearchResult> list() {
			if (mResponse == null || mResponse.games == null) {
				return new ArrayList<SearchResult>();
			}
			return mResponse.games;
		}
	}

	public static class SearchResultsAdapter extends ArrayAdapter<SearchResult> {
		private LayoutInflater mInflater;
		private Resources mResources;

		public SearchResultsAdapter(Activity activity, List<SearchResult> results) {
			super(activity, R.layout.row_search, results);
			mInflater = activity.getLayoutInflater();
			mResources = activity.getResources();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row_search, parent, false);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SearchResult game = null;
			try {
				game = getItem(position);
			} catch (ArrayIndexOutOfBoundsException e) {
				return convertView;
			}
			if (game != null) {
				holder.name.setText(game.name);
				int style = Typeface.NORMAL;
				switch (game.getNameType()) {
					case SearchResult.NAME_TYPE_PRIMARY:
						style = Typeface.BOLD;
						break;
					case SearchResult.NAME_TYPE_ALTERNATE:
						style = Typeface.ITALIC;
						break;
				}
				holder.name.setTypeface(holder.name.getTypeface(), style);
				if (game.yearPublished > 0) {
					holder.year.setText("" + game.yearPublished);
				}
				holder.gameId.setText(String.format(mResources.getString(R.string.id_list_text), game.id));
			}

			return convertView;
		}
	}

	static class ViewHolder {
		TextView name;
		TextView year;
		TextView gameId;

		public ViewHolder(View view) {
			name = (TextView) view.findViewById(R.id.name);
			year = (TextView) view.findViewById(R.id.year);
			gameId = (TextView) view.findViewById(R.id.gameId);
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.game_context, menu);
		mLogPlayMenuItem = menu.findItem(R.id.menu_log_play);
		mLogPlayQuickMenuItem = menu.findItem(R.id.menu_log_play_quick);
		mBggLinkMenuItem = menu.findItem(R.id.menu_link);
		mSelectedPositions.clear();
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		if (checked) {
			mSelectedPositions.add(position);
		} else {
			mSelectedPositions.remove(position);
		}

		int count = mSelectedPositions.size();
		mode.setTitle(getResources().getQuantityString(R.plurals.msg_games_selected, count, count));

		mLogPlayMenuItem.setVisible(count == 1 && PreferencesUtils.showLogPlay(getActivity()));
		mLogPlayQuickMenuItem.setVisible(count == 1 && PreferencesUtils.showQuickLogPlay(getActivity()));
		mBggLinkMenuItem.setVisible(count == 1);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		SearchResult game = (SearchResult) mAdapter.getItem(mSelectedPositions.iterator().next());
		switch (item.getItemId()) {
			case R.id.menu_log_play:
				mode.finish();
				ActivityUtils.logPlay(getActivity(), game.id, game.name, null, null, false);
				return true;
			case R.id.menu_log_play_quick:
				mode.finish();
				Toast.makeText(getActivity(), R.string.msg_logging_play, Toast.LENGTH_SHORT).show();
				ActivityUtils.logQuickPlay(getActivity(), game.id, game.name);
				return true;
			case R.id.menu_share:
				mode.finish();
				if (mSelectedPositions.size() == 1) {
					ActivityUtils.shareGame(getActivity(), game.id, game.name);
				} else {
					List<Pair<Integer, String>> games = new ArrayList<Pair<Integer, String>>(mSelectedPositions.size());
					for (int position : mSelectedPositions) {
						SearchResult g = (SearchResult) mAdapter.getItem(position);
						games.add(new Pair<Integer, String>(g.id, g.name));
					}
					ActivityUtils.shareGames(getActivity(), games);
				}
				return true;
			case R.id.menu_link:
				mode.finish();
				ActivityUtils.linkBgg(getActivity(), game.id);
				return true;
		}
		return false;
	}
}
