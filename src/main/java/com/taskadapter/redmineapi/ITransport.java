package com.taskadapter.redmineapi;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;

import com.taskadapter.redmineapi.bean.Identifiable;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport.ResultsWrapper;
import com.taskadapter.redmineapi.internal.comm.BasicHttpResponse;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;

public interface ITransport extends Closeable {

	/**
	 * Performs an "add child object" request.
	 *
	 * @param parentClass
	 *            parent object id.
	 * @param object
	 *            object to use.
	 * @param params
	 *            name params.
	 * @return object to use.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	<T> T addChildEntry(Class<?> parentClass, String parentId, T object, NameValuePair... params)
			throws RedmineException;

	/**
	 * Performs an "add object" request.
	 *
	 * @param object
	 *            object to use.
	 * @param params
	 *            name params.
	 * @return object to use.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	<T> T addObject(T object, NameValuePair... params) throws RedmineException;

	void addUserToGroup(int userId, int groupId) throws RedmineException;

	void addWatcherToIssue(int watcherId, int issueId) throws RedmineException;

	/**
	 * Performs "delete child Id" request.
	 *
	 * @param parentClass
	 *            parent object id.
	 * @param object
	 *            object to use.
	 * @param value
	 *            child object id.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	<T> void deleteChildId(Class<?> parentClass, String parentId, T object, Integer value) throws RedmineException;

	/**
	 * Deletes an object.
	 *
	 * @param classs
	 *            object class.
	 * @param id
	 *            object id.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	<T extends Identifiable> void deleteObject(Class<T> classs, String id) throws RedmineException;

	/**
	 * Downloads redmine content.
	 *
	 * @param uri
	 *            target uri.
	 * @param handler
	 *            content handler.
	 * @return handler result.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	<R> R download(String uri, ContentHandler<BasicHttpResponse, R> handler) throws RedmineException;

	<T> List<T> getChildEntries(Class<?> parentClass, int parentId, Class<T> classs) throws RedmineException;

	/**
	 * Delivers a list of a child entries.
	 *
	 * @param classs
	 *            target class.
	 */
	<T> List<T> getChildEntries(Class<?> parentClass, String parentKey, Class<T> classs) throws RedmineException;

	/**
	 * Delivers a single child entry by its identifier.
	 */
	<T> T getChildEntry(Class<?> parentClass, String parentId, Class<T> classs, String childId, NameValuePair... params)
			throws RedmineException;

	User getCurrentUser(NameValuePair... params) throws RedmineException;

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param args
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	<T> T getObject(Class<T> classs, Integer key, NameValuePair... args) throws RedmineException;

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param args
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	<T> T getObject(Class<T> classs, String key, NameValuePair... args) throws RedmineException;

	/**
	 * Returns all objects found using the provided parameters.
	 * This method IGNORES "limit" and "offset" parameters and handles paging AUTOMATICALLY for you.
	 * Please use getObjectsListNoPaging() method if you want to control paging yourself with "limit" and "offset" parameters.
	 *
	 * @return objects list, never NULL
	 *
	 * @see #getObjectsListNoPaging(Class, Collection)
	 */
	<T> List<T> getObjectsList(Class<T> objectClass, Collection<? extends NameValuePair> params)
			throws RedmineException;

	<T> List<T> getObjectsList(Class<T> objectClass, NameValuePair... params) throws RedmineException;

	/**
	 * Returns an object list. Provide your own "limit" and "offset" parameters if you need those, otherwise
	 * this method will return the first page of some default size only (this default is controlled by
	 * your Redmine configuration).
	 *
	 * @return objects list, never NULL
	 */
	<T> ResultsWrapper<T> getObjectsListNoPaging(Class<T> objectClass, Collection<? extends NameValuePair> params)
			throws RedmineException;

	void setCredentials(String login, String password);

	void setLogin(String login);

	/**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	void setObjectsPerPage(int pageSize);

	/**
	 * This works only when the main authentication has led to Redmine Admin level user.
	 * The given user name will be sent to the server in "X-Redmine-Switch-User" HTTP Header
	 * to indicate that the action (create issue, delete issue, etc) must be done
	 * on behalf of the given user name.
	 *
	 * @param loginName Redmine user login name to provide to the server
	 *
	 * @see <a href="http://www.redmine.org/issues/11755">Redmine issue 11755</a>
	 */
	void setOnBehalfOfUser(String loginName);

	void setPassword(String password);

	/*
	 * note: This method cannot return the updated object from Redmine because
	 * the server does not provide any XML in response.
	 *
	 * @since 1.8.0
	 */
	<T extends Identifiable> void updateObject(T obj, NameValuePair... params) throws RedmineException;

	/**
	 * UPloads content on a server.
	 *
	 * @param content
	 *            content stream.
	 * @return uploaded item token.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	String upload(InputStream content) throws RedmineException;
	
	/**
	 * UPloads content on a server , an archive of a given size
	 * 
	 * @param _wrapper
	 *            the stream
	 * @param _size
	 *            the stream size
	 *			
	 * @param content
	 *            content stream.
	 * @return uploaded item token.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	String upload(InputStream _wrapper, long _size) throws RedmineException;

}