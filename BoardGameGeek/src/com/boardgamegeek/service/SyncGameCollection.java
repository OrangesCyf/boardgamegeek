package com.boardgamegeek.service;

import static com.boardgamegeek.util.LogUtils.LOGI;
import static com.boardgamegeek.util.LogUtils.makeLogTag;

import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.content.Context;

import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.io.Adapter;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.model.CollectionResponse;
import com.boardgamegeek.model.persister.CollectionPersister;

public class SyncGameCollection extends UpdateTask {
	private static final String TAG = makeLogTag(SyncGameCollection.class);
	private static final String STATUS_PLAYED = "played";

	private int mGameId;

	public SyncGameCollection(int gameId) {
		mGameId = gameId;
	}

	@Override
	public String getDescription() {
		return "Sync collection for game ID=" + mGameId;
	}

	@Override
	public void execute(Context context) {
		Account account = Authenticator.getAccount(context);
		if (account == null) {
			return;
		}

		CollectionPersister persister = new CollectionPersister(context).includePrivateInfo().includeStats();
		BggService service = Adapter.createWithAuth(context);

		Map<String, String> options = new HashMap<String, String>();
		options.put(BggService.COLLECTION_QUERY_KEY_SHOW_PRIVATE, "1");
		options.put(BggService.COLLECTION_QUERY_KEY_STATS, "1");
		options.put(BggService.COLLECTION_QUERY_KEY_ID, String.valueOf(mGameId));
		if (requestAndPersist(account, persister, service, options)) {
			return;
		}

		options.put(STATUS_PLAYED, "1");
		if (requestAndPersist(account, persister, service, options)) {
			return;
		}

		options.remove(STATUS_PLAYED);
		options.put(BggService.COLLECTION_QUERY_KEY_SUBTYPE, BggService.THING_SUBTYPE_BOARDGAME_ACCESSORY);
		if (requestAndPersist(account, persister, service, options)) {
			return;
		}

		options.put(STATUS_PLAYED, "1");
		if (requestAndPersist(account, persister, service, options)) {
			return;
		}
	}

	private boolean requestAndPersist(Account account, CollectionPersister persister, BggService service,
		Map<String, String> options) {
		CollectionResponse response = getCollectionResponse(service, account.name, options);
		if (response == null || response.items == null || response.items.size() == 0) {
			LOGI(TAG, "No collection items for game ID=" + mGameId);
			return false;
		} else {
			persister.save(response.items);
			LOGI(TAG, "Synced " + response.items.size() + " collection item(s) for game ID=" + mGameId);
			return true;
		}
	}
}
