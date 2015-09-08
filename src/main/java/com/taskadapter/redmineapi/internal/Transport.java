package com.taskadapter.redmineapi.internal;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.ITransport;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Identifiable;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.News;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import com.taskadapter.redmineapi.internal.comm.BaseCommunicator;
import com.taskadapter.redmineapi.internal.comm.BasicHttpResponse;
import com.taskadapter.redmineapi.internal.comm.Communicator;
import com.taskadapter.redmineapi.internal.comm.Communicators;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;
import com.taskadapter.redmineapi.internal.comm.SimpleCommunicator;
import com.taskadapter.redmineapi.internal.comm.redmine.RedmineAuthenticator;
import com.taskadapter.redmineapi.internal.comm.redmine.RedmineErrorHandler;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import com.taskadapter.redmineapi.internal.json.JsonObjectParser;
import com.taskadapter.redmineapi.internal.json.JsonObjectWriter;

public final class Transport implements ITransport {
	public static class ResultsWrapper<T> {
		final private Integer totalFoundOnServer;
		final private List<T> results;
		
		public ResultsWrapper(final Integer totalFoundOnServer, final List<T> results) {
			this.totalFoundOnServer = totalFoundOnServer;
			this.results = results;
		}
		
		public List<T> getResults() {
			return results;
		}
		
		public int getResultsNumber() {
			return results.size();
		}
		
		public Integer getTotalFoundOnServer() {
			return totalFoundOnServer;
		}
		
		public boolean hasSomeResults() {
			return !results.isEmpty();
		}
	}
	/**
	 * Entity config.
	 */
	static class EntityConfig<T> {
		final String singleObjectName;
		final String multiObjectName;
		final JsonObjectWriter<T> writer;
		final JsonObjectParser<T> parser;
		
		public EntityConfig(final String objectField, final String urlPrefix,
				final JsonObjectWriter<T> writer, final JsonObjectParser<T> parser) {
			super();
			this.singleObjectName = objectField;
			this.multiObjectName = urlPrefix;
			this.writer = writer;
			this.parser = parser;
		}
	}
	private static final Map<Class<?>, EntityConfig<?>> OBJECT_CONFIGS = new HashMap<Class<?>, EntityConfig<?>>();
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	
	private static final int DEFAULT_OBJECTS_PER_PAGE = 25;
	private static final String KEY_TOTAL_COUNT = "total_count";
	static {
		OBJECT_CONFIGS.put(
				Project.class,
				config("project", "projects",
						RedmineJSONBuilder.PROJECT_WRITER,
						RedmineJSONParser.PROJECT_PARSER));
		OBJECT_CONFIGS.put(
				Issue.class,
				config("issue", "issues", RedmineJSONBuilder.ISSUE_WRITER,
						RedmineJSONParser.ISSUE_PARSER));
		OBJECT_CONFIGS.put(
				User.class,
				config("user", "users", RedmineJSONBuilder.USER_WRITER,
						RedmineJSONParser.USER_PARSER));
		OBJECT_CONFIGS.put(
				Group.class,
				config("group", "groups", RedmineJSONBuilder.GROUP_WRITER,
						RedmineJSONParser.GROUP_PARSER));
		OBJECT_CONFIGS.put(
				IssueCategory.class,
				config("issue_category", "issue_categories",
						RedmineJSONBuilder.CATEGORY_WRITER,
						RedmineJSONParser.CATEGORY_PARSER));
		OBJECT_CONFIGS.put(
				Version.class,
				config("version", "versions",
						RedmineJSONBuilder.VERSION_WRITER,
						RedmineJSONParser.VERSION_PARSER));
		OBJECT_CONFIGS.put(
				TimeEntry.class,
				config("time_entry", "time_entries",
						RedmineJSONBuilder.TIME_ENTRY_WRITER,
						RedmineJSONParser.TIME_ENTRY_PARSER));
		OBJECT_CONFIGS.put(News.class,
				config("news", "news", null, RedmineJSONParser.NEWS_PARSER));
		OBJECT_CONFIGS.put(
				IssueRelation.class,
				config("relation", "relations",
						RedmineJSONBuilder.RELATION_WRITER,
						RedmineJSONParser.RELATION_PARSER));
		OBJECT_CONFIGS.put(
				Tracker.class,
				config("tracker", "trackers", null,
						RedmineJSONParser.TRACKER_PARSER));
		OBJECT_CONFIGS.put(
				IssueStatus.class,
				config("status", "issue_statuses", null,
						RedmineJSONParser.STATUS_PARSER));
		OBJECT_CONFIGS
		.put(SavedQuery.class,
				config("query", "queries", null,
						RedmineJSONParser.QUERY_PARSER));
		OBJECT_CONFIGS.put(Role.class,
				config("role", "roles", null, RedmineJSONParser.ROLE_PARSER));
		OBJECT_CONFIGS.put(
				Membership.class,
				config("membership", "memberships",
						RedmineJSONBuilder.MEMBERSHIP_WRITER,
						RedmineJSONParser.MEMBERSHIP_PARSER));
		OBJECT_CONFIGS.put(
				IssuePriority.class,
				config("issue_priority", "issue_priorities", null,
						RedmineJSONParser.ISSUE_PRIORITY_PARSER));
		OBJECT_CONFIGS.put(
				TimeEntryActivity.class,
				config("time_entry_activity", "time_entry_activities", null,
						RedmineJSONParser.TIME_ENTRY_ACTIVITY_PARSER));
		
		OBJECT_CONFIGS.put(
				Watcher.class,
				config("watcher", "watchers", null,
						RedmineJSONParser.WATCHER_PARSER));
		
		OBJECT_CONFIGS.put(
				WikiPage.class,
				config("wiki_page", "wiki_pages", null, RedmineJSONParser.WIKI_PAGE_PARSER)
				);
		
		OBJECT_CONFIGS.put(
				WikiPageDetail.class,
				config("wiki_page", null, null, RedmineJSONParser.WIKI_PAGE_DETAIL_PARSER)
				);
		OBJECT_CONFIGS.put(
				CustomFieldDefinition.class,
				config("custom_field", "custom_fields", null,
						RedmineJSONParser.CUSTOM_FIELD_DEFINITION_PARSER));
	}
	private static final String CHARSET = "UTF-8";
	
	private static <T> EntityConfig<T> config(final String objectField,
			final String urlPrefix, final JsonObjectWriter<T> writer,
			final JsonObjectParser<T> parser) {
		return new EntityConfig<T>(objectField, urlPrefix, writer, parser);
	}
	
	private static <T> T parseResponse(final String response, final String tag,
			final JsonObjectParser<T> parser) throws RedmineFormatException {
		try {
			return parser.parse(RedmineJSONParser.getResponseSingleObject(response, tag));
		} catch (final JSONException e) {
			throw new RedmineFormatException(e);
		}
	}
	
	private static void setEntity(final HttpEntityEnclosingRequest request, final String body) {
		setEntity(request, body, CONTENT_TYPE);
	}
	private static void setEntity(final HttpEntityEnclosingRequest request, final String body, final String contentType) {
		StringEntity entity;
		try {
			entity = new StringEntity(body, CHARSET);
		} catch (final UnsupportedCharsetException e) {
			throw new RedmineInternalError("Required charset " + CHARSET
					+ " is not supported", e);
		}
		entity.setContentType(contentType);
		request.setEntity(entity);
	}
	private final Logger logger = LoggerFactory.getLogger(RedmineManager.class);
	private final SimpleCommunicator<String> communicator;
	private final Communicator<BasicHttpResponse> errorCheckingCommunicator;
	
	private final RedmineAuthenticator<HttpResponse> authenticator;
	
	private String onBehalfOfUser = null;
	
	private final URIConfigurator configurator;
	
	private String login;
	
	private String password;
	
	private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;
	
	public Transport(final URIConfigurator configurator, final HttpClient client) {
		this.configurator = configurator;
		final Communicator<HttpResponse> baseCommunicator = new BaseCommunicator(client);
		authenticator = new RedmineAuthenticator<HttpResponse>(
				baseCommunicator, CHARSET);
		final ContentHandler<BasicHttpResponse, BasicHttpResponse> errorProcessor = new RedmineErrorHandler();
		errorCheckingCommunicator = Communicators.fmap(
				authenticator,
				Communicators.compose(errorProcessor,
						Communicators.transportDecoder()));
		final Communicator<String> coreCommunicator = Communicators.fmap(errorCheckingCommunicator,
				Communicators.contentReader());
		communicator = Communicators.simplify(coreCommunicator,
				Communicators.<String>identityHandler());
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#addChildEntry(java.lang.Class, java.lang.String, T, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> T addChildEntry(final Class<?> parentClass, final String parentId, final T object,
			final NameValuePair... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		final URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentId, object.getClass(), params);
		final HttpPost httpPost = new HttpPost(uri);
		final String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName,
				object, config.writer);
		setEntity(httpPost, body);
		final String response = send(httpPost);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#addObject(T, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> T addObject(final T object, final NameValuePair... params)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		if (config.writer == null) {
			throw new RuntimeException("can't create object: writer is not implemented or is not registered in RedmineJSONBuilder for object " + object);
		}
		final URI uri = getURIConfigurator().getObjectsURI(object.getClass(), params);
		final HttpPost httpPost = new HttpPost(uri);
		final String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName, object, config.writer);
		setEntity(httpPost, body);
		final String response = send(httpPost);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#addUserToGroup(int, int)
	 */
	@Override
	public void addUserToGroup(final int userId, final int groupId) throws RedmineException {
		logger.debug("adding user " + userId + " to group " + groupId + "...");
		final URI uri = getURIConfigurator().getChildObjectsURI(Group.class, Integer.toString(groupId), User.class);
		final HttpPost httpPost = new HttpPost(uri);
		final StringWriter writer = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(writer);
		try {
			jsonWriter.object().key("user_id").value(userId).endObject();
		} catch (final JSONException e) {
			throw new RedmineInternalError("Unexpected exception", e);
		}
		final String body = writer.toString();
		setEntity(httpPost, body);
		final String response = send(httpPost);
		logger.debug(response);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#addWatcherToIssue(int, int)
	 */
	@Override
	public void addWatcherToIssue(final int watcherId, final int issueId) throws RedmineException {
		logger.debug("adding watcher " + watcherId + " to issue " + issueId + "...");
		final URI uri = getURIConfigurator().getChildObjectsURI(Issue.class, Integer.toString(issueId), Watcher.class);
		final HttpPost httpPost = new HttpPost(uri);
		final StringWriter writer = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(writer);
		try {
			jsonWriter.object().key("user_id").value(watcherId).endObject();
		} catch (final JSONException e) {
			throw new RedmineInternalError("Unexpected exception", e);
		}
		final String body = writer.toString();
		setEntity(httpPost, body);
		final String response = send(httpPost);
		logger.debug(response);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#deleteChildId(java.lang.Class, java.lang.String, T, java.lang.Integer)
	 */
	@Override
	public <T> void deleteChildId(final Class<?> parentClass, final String parentId, final T object, final Integer value) throws RedmineException {
		final URI uri = getURIConfigurator().getChildIdURI(parentClass, parentId, object.getClass(), value);
		final HttpDelete httpDelete = new HttpDelete(uri);
		final String response = send(httpDelete);
		logger.debug(response);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#deleteObject(java.lang.Class, java.lang.String)
	 */
	@Override
	public <T extends Identifiable> void deleteObject(final Class<T> classs, final String id)
			throws RedmineException {
		final URI uri = getURIConfigurator().getObjectURI(classs, id);
		final HttpDelete http = new HttpDelete(uri);
		send(http);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#download(java.lang.String, com.taskadapter.redmineapi.internal.comm.ContentHandler)
	 */
	@Override
	public <R> R download(final String uri,
			final ContentHandler<BasicHttpResponse, R> handler)
					throws RedmineException {
		final HttpGet request = new HttpGet(uri);
		if (onBehalfOfUser != null) {
			request.addHeader("X-Redmine-Switch-User", onBehalfOfUser);
		}
		return errorCheckingCommunicator.sendRequest(request, handler);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getChildEntries(java.lang.Class, int, java.lang.Class)
	 */
	@Override
	public <T> List<T> getChildEntries(final Class<?> parentClass, final int parentId, final Class<T> classs) throws RedmineException {
		return getChildEntries(parentClass, parentId + "", classs);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getChildEntries(java.lang.Class, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> List<T> getChildEntries(final Class<?> parentClass, final String parentKey, final Class<T> classs) throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentKey, classs, new BasicNameValuePair("limit", String.valueOf(objectsPerPage)));
		
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		final JSONObject responseObject;
		try {
			responseObject = RedmineJSONParser.getResponse(response);
			return JsonInput.getListNotNull(responseObject, config.multiObjectName, config.parser);
		} catch (final JSONException e) {
			throw new RedmineFormatException("Bad categories response " + response, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getChildEntry(java.lang.Class, java.lang.String, java.lang.Class, java.lang.String, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> T getChildEntry(final Class<?> parentClass, final String parentId,
			final Class<T> classs, final String childId, final NameValuePair... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getChildIdURI(parentClass, parentId, classs, childId, params);
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		
		return parseResponse(response, config.singleObjectName, config.parser);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getCurrentUser(org.apache.http.NameValuePair)
	 */
	@Override
	public User getCurrentUser(final NameValuePair... params) throws RedmineException {
		final URI uri = getURIConfigurator().createURI("users/current.json", params);
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		return parseResponse(response, "user", RedmineJSONParser.USER_PARSER);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getObject(java.lang.Class, java.lang.Integer, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> T getObject(final Class<T> classs, final Integer key, final NameValuePair... args) throws RedmineException {
		return getObject(classs, key.toString(), args);
	}

	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getObject(java.lang.Class, java.lang.String, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> T getObject(final Class<T> classs, final String key, final NameValuePair... args)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getObjectURI(classs, key, args);
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getObjectsList(java.lang.Class, java.util.Collection)
	 */
	@Override
	public <T> List<T> getObjectsList(final Class<T> objectClass,
			final Collection<? extends NameValuePair> params) throws RedmineException {
		final List<T> result = new ArrayList<T>();
		int offset = 0;
		
		Integer totalObjectsFoundOnServer;
		do {
			final List<NameValuePair> newParams = new ArrayList<NameValuePair>(params);
			newParams.add(new BasicNameValuePair("limit", String.valueOf(objectsPerPage)));
			newParams.add(new BasicNameValuePair("offset", String.valueOf(offset)));
			
			final ResultsWrapper<T> wrapper = getObjectsListNoPaging(objectClass, newParams);
			result.addAll(wrapper.getResults());
			
			totalObjectsFoundOnServer = wrapper.getTotalFoundOnServer();
			// Necessary for trackers.
			// TODO Alexey: is this still necessary for Redmine 2.x?
			if (totalObjectsFoundOnServer == null) {
				break;
			}
			if (!wrapper.hasSomeResults()) {
				break;
			}
			offset += wrapper.getResultsNumber();
		} while (offset < totalObjectsFoundOnServer);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getObjectsList(java.lang.Class, org.apache.http.NameValuePair)
	 */
	@Override
	public <T> List<T> getObjectsList(final Class<T> objectClass, final NameValuePair... params) throws RedmineException {
		return getObjectsList(objectClass, Arrays.asList(params));
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#getObjectsListNoPaging(java.lang.Class, java.util.Collection)
	 */
	@Override
	public <T> ResultsWrapper<T> getObjectsListNoPaging(final Class<T> objectClass,
			final Collection<? extends NameValuePair> params) throws RedmineException {
		final EntityConfig<T> config = getConfig(objectClass);
		final List<NameValuePair> newParams = new ArrayList<NameValuePair>(params);
		final List<NameValuePair> paramsList = new ArrayList<NameValuePair>(newParams);
		final URI uri = getURIConfigurator().getObjectsURI(objectClass, paramsList);
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		try {
			final JSONObject responseObject = RedmineJSONParser.getResponse(response);
			final List<T> results = JsonInput.getListOrNull(responseObject, config.multiObjectName, config.parser);
			final Integer totalFoundOnServer = JsonInput.getIntOrNull(responseObject, KEY_TOTAL_COUNT);
			return new ResultsWrapper<T>(totalFoundOnServer, results);
		} catch (final JSONException e) {
			throw new RedmineFormatException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#setCredentials(java.lang.String, java.lang.String)
	 */
	@Override
	public void setCredentials(final String login, final String password) {
		this.login = login;
		this.password = password;
		authenticator.setCredentials(login, password);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#setLogin(java.lang.String)
	 */
	@Override
	public void setLogin(final String login) {
		setCredentials(login, password);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#setObjectsPerPage(int)
	 */
	@Override
	public void setObjectsPerPage(final int pageSize) {
		if (pageSize <= 0) {
			throw new IllegalArgumentException("Page size must be >= 0. You provided: " + pageSize);
		}
		objectsPerPage = pageSize;
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#setOnBehalfOfUser(java.lang.String)
	 */
	@Override
	public void setOnBehalfOfUser(final String loginName) {
		onBehalfOfUser = loginName;
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#setPassword(java.lang.String)
	 */
	@Override
	public void setPassword(final String password) {
		setCredentials(login, password);
	}
	
	/*
	 * note: This method cannot return the updated object from Redmine because
	 * the server does not provide any XML in response.
	 *
	 * @since 1.8.0
	 */
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#updateObject(T, org.apache.http.NameValuePair)
	 */
	@Override
	public <T extends Identifiable> void updateObject(final T obj,
			final NameValuePair... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(obj.getClass());
		final Integer id = obj.getId();
		if (id == null) {
			throw new RuntimeException("'id' field cannot be NULL in the given object:" +
					" it is required to identify the object in the target system");
		}
		final URI uri = getURIConfigurator().getObjectURI(obj.getClass(),
				Integer.toString(id));
		final HttpPut http = new HttpPut(uri);
		final String body = RedmineJSONBuilder.toSimpleJSON(
				config.singleObjectName, obj, config.writer);
		setEntity(http, body);
		send(http);
	}
	
	/* (non-Javadoc)
	 * @see com.taskadapter.redmineapi.internal.ITransport#upload(java.io.InputStream)
	 */
	@Override
	public String upload(final InputStream content) throws RedmineException {
		final URI uploadURI = getURIConfigurator().getUploadURI();
		final HttpPost request = new HttpPost(uploadURI);
		final AbstractHttpEntity entity = new InputStreamEntity(content, -1);
		/* Content type required by a Redmine */
		entity.setContentType("application/octet-stream");
		request.setEntity(entity);
		
		final String result = send(request);
		return parseResponse(result, "upload", RedmineJSONParser.UPLOAD_TOKEN_PARSER);
	}
	
	@SuppressWarnings("unchecked")
	private <T> EntityConfig<T> getConfig(final Class<?> class1) {
		final EntityConfig<?> guess = OBJECT_CONFIGS.get(class1);
		if (guess == null) {
			throw new RedmineInternalError("Unsupported class " + class1);
		}
		return (EntityConfig<T>) guess;
	}
	
	private URIConfigurator getURIConfigurator() {
		return configurator;
	}
	
	private String send(final HttpRequestBase http) throws RedmineException {
		if (onBehalfOfUser != null) {
			http.addHeader("X-Redmine-Switch-User", onBehalfOfUser);
		}
		return communicator.sendRequest(http);
	}
	
}
