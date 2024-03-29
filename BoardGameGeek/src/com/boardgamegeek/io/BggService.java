package com.boardgamegeek.io;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

import com.boardgamegeek.model.CollectionResponse;
import com.boardgamegeek.model.Company;
import com.boardgamegeek.model.ForumListResponse;
import com.boardgamegeek.model.ForumResponse;
import com.boardgamegeek.model.HotnessResponse;
import com.boardgamegeek.model.Person;
import com.boardgamegeek.model.PlayPostResponse;
import com.boardgamegeek.model.PlaysResponse;
import com.boardgamegeek.model.SearchResponse;
import com.boardgamegeek.model.ThingResponse;
import com.boardgamegeek.model.ThreadResponse;
import com.boardgamegeek.model.User;

public interface BggService {
	public static final String HOTNESS_TYPE_BOARDGAME = "boardgame";
	// rpg
	// videogame
	// boardgameperson
	// rpgperson
	// boardgamecompany
	// rpgcompany
	// videogamecompany
	public static final String THING_SUBTYPE_BOARDGAME_ACCESSORY = "boardgameaccessory";

	public static final String PERSON_TYPE_ARTIST = "boardgameartist";
	public static final String PERSON_TYPE_DESIGNER = "boardgamedesigner";
	public static final String COMPANY_TYPE_PUBLISHER = "boardgamepublisher";
	public static final String SEARCH_TYPE_BOARD_GAME = "boardgame";
	public static final String SEARCH_TYPE_BOARD_GAME_EXPANSION = "boardgameexpansion";
	public static final String SEARCH_TYPE_RPG = "rpg";
	public static final String SEARCH_TYPE_RPG_ITEM = "rpgitem";
	public static final String SEARCH_TYPE_VIDEO_GAME = "videogame";
	// other search types: boardgameartist, boardgamedesigner, boardgamepublisher

	public static String FORUM_TYPE_REGION = "region";
	public static String FORUM_TYPE_THING = "thing";

	public static final int FORUM_REGION_BOARDGAME = 1;
	public static final int FORUM_REGION_RPG = 2;
	public static final int FORUM_REGION_VIDEOGAME = 3;

	public static final String COLLECTION_QUERY_KEY_ID = "id";
	public static final String COLLECTION_QUERY_KEY_SHOW_PRIVATE = "showprivate";
	public static final String COLLECTION_QUERY_KEY_STATS = "stats";
	public static final String COLLECTION_QUERY_KEY_MODIFIED_SINCE = "modifiedsince";
	public static final String COLLECTION_QUERY_KEY_BRIEF = "brief";
	public static final String COLLECTION_QUERY_KEY_SUBTYPE = "subtype";
	public static final SimpleDateFormat COLLECTION_QUERY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	// TODO: add HH:MM:SS

	@GET("/xmlapi2/hot")
	HotnessResponse getHotness(@Query("type") String type);

	@GET("/xmlapi2/forumlist")
	ForumListResponse forumList(@Query("type") String type, @Query("id") int id);

	@GET("/xmlapi2/forum")
	ForumResponse forum(@Query("id") int id, @Query("page") int page);

	// minarticleid=NNN Filters the results so that only articles with an equal or higher id than NNN will be returned.
	// minarticledate=YYYY-MM-DD Filters the results so that only articles on the specified date or later will be
	// returned.
	// minarticledate=YYYY-MM-DD%20HH%3AMM%3ASS Filters the results so that only articles after the specified date an
	// time (HH:MM:SS) or later will be returned.
	// count=NNN Limits the number of articles returned to no more than NNN.
	// username=NAME

	@GET("/xmlapi2/thread")
	ThreadResponse thread(@Query("id") int id);

	@GET("/xmlapi/{type}/{id}")
	Person person(@Path("type") String type, @Path("id") int id);

	@GET("/xmlapi/{type}/{id}")
	Company company(@Path("type") String type, @Path("id") int id);

	@GET("/xmlapi2/search")
	SearchResponse search(@Query("query") String query, @Query("type") String type, @Query("exact") int exact);

	// username=NAME Name of the player you want to request play information for. Data is returned in
	// backwards-chronological form. You must include either a username or an id and type to get results.
	// id=NNN Id number of the item you want to request play information for. Data is returned in
	// backwards-chronological form.
	// type=TYPE Type of the item you want to request play information for. Valid types include:
	// thing
	// family
	// mindate=YYYY-MM-DD Returns only plays of the specified date or later.
	// maxdate=YYYY-MM-DD Returns only plays of the specified date or earlier.
	// subtype=TYPE Limits play results to the specified TYPE; boardgame is the default. Valid types include:
	// boardgame
	// boardgameexpansion
	// rpgitem
	// videogame
	// page=NNN The page of information to request. Page size is 100 records.
	@GET("/xmlapi2/plays")
	PlaysResponse plays(@QueryMap Map<String, String> options);

	@GET("/xmlapi2/plays")
	PlaysResponse playsByDate(@Query("username") String username, @Query("mindate") String minDate,
		@Query("maxdate") String maxDate);

	@GET("/xmlapi2/plays")
	PlaysResponse playsByGame(@Query("username") String username, @Query("id") int gameId);

	@GET("/xmlapi2/plays")
	PlaysResponse plays(@Query("username") String username, @Query("id") int gameId, @Query("mindate") String minDate,
		@Query("maxdate") String maxDate);

	@GET("/xmlapi2/plays")
	PlaysResponse playsByMinDate(@Query("username") String username, @Query("mindate") String minDate,
		@Query("page") int page);

	@GET("/xmlapi2/plays")
	PlaysResponse playsByMaxDate(@Query("username") String username, @Query("maxdate") String maxDate,
		@Query("page") int page);

	@GET("/xmlapi2/plays")
	PlaysResponse plays(@Query("username") String username, @Query("page") int page);

	@GET("/xmlapi2/user")
	User user(@Query("name") String name);

	@GET("/xmlapi2/user")
	User user(@Query("name") String name, @Query("buddies") int buddies, @Query("page") int page);

	@GET("/xmlapi2/collection")
	CollectionResponse collection(@Query("username") String username, @QueryMap Map<String, String> options);

	@GET("/xmlapi2/thing")
	ThingResponse thing(@Query("id") int gameId, @Query("stats") int stats);

	@GET("/xmlapi2/thing")
	ThingResponse thing(@Query("id") String gameIds, @Query("stats") int stats);

	@GET("/xmlapi2/thing?comments=1")
	ThingResponse thingWithComments(@Query("id") int gameId, @Query("page") int page);

	@GET("/xmlapi2/thing?ratingcomments=1")
	ThingResponse thingWithRatings(@Query("id") int gameId, @Query("page") int page);

	@FormUrlEncoded
	@POST("/geekplay.php")
	PlayPostResponse geekPlay(@FieldMap Map<String, String> form);
}
