package com.taskadapter.redmineapi;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.taskadapter.redmineapi.internal.Transport;
import com.taskadapter.redmineapi.internal.URIConfigurator;

/**
 * <strong>Entry point</strong> for the API. Use this class to communicate with
 * Redmine servers.
 * <p>
 * Collection of creation methods for the redmine. Method number may grow as
 * grows number of requirements. However, having all creation methods in one
 * place allows us to refactor RemineManager internals without changing this
 * external APIs. Moreover, we can create "named constructor" for redmine
 * instances. This will allow us to have many construction methods with the same
 * signature.
 * <p>
 * Sample usage:
 *
 * <pre>
 * RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 * </pre>
 *
 * @see RedmineManager
 */
public final class RedmineManagerFactory {

	/**
	 * Creates a non-authenticating redmine manager.
	 *
	 * @param uri
	 *            redmine manager URI.
	 */
	public static RedmineManager createUnauthenticated(final String uri) {
		return createUnauthenticated(uri, createDefaultTransportConfig());
	}

	/**
	 * Creates a non-authenticating redmine manager.
	 *
	 * @param uri
	 *            redmine manager URI.
	 * @param config
	 *            transport configuration.
	 */
	public static RedmineManager createUnauthenticated(final String uri, final TransportConfiguration config) {
		return createWithUserAuth(uri, null, null, config);
	}

	/**
	 * Creates an instance of RedmineManager class. Host and apiAccessKey are
	 * not checked at this moment.
	 *
	 * @param uri
	 *            complete Redmine server web URI, including protocol and port
	 *            number. Example: http://demo.redmine.org:8080
	 * @param apiAccessKey
	 *            Redmine API access key. It is shown on "My Account" /
	 *            "API access key" webpage (check
	 *            <i>http://redmine_server_url/my/account</i> URL). This
	 *            parameter is <strong>optional</strong> (can be set to NULL)
	 *            for Redmine projects, which are "public".
	 */
	public static RedmineManager createWithApiKey(final String uri, final String apiAccessKey) {
		return createWithApiKey(uri, apiAccessKey, createDefaultTransportConfig());
	}

	/**
	 * Creates an instance of RedmineManager class. Host and apiAccessKey are
	 * not checked at this moment.
	 *
	 * @param uri
	 *            complete Redmine server web URI, including protocol and port
	 *            number. Example: http://demo.redmine.org:8080
	 * @param apiAccessKey
	 *            Redmine API access key. It is shown on "My Account" /
	 *            "API access key" webpage (check
	 *            <i>http://redmine_server_url/my/account</i> URL). This
	 *            parameter is <strong>optional</strong> (can be set to NULL)
	 *            for Redmine projects, which are "public".
	 * @param config
	 *            transport configuration.
	 */
	public static RedmineManager createWithApiKey(final String uri, final String apiAccessKey,
			final TransportConfiguration config) {
		return new RedmineManager(new Transport(new URIConfigurator(uri, apiAccessKey), config.client),
				config.shutdownListener);
	}

	/**
	 * Creates a new RedmineManager with user-based authentication.
	 *
	 * @param uri
	 *            redmine manager URI.
	 * @param login
	 *            user's name.
	 * @param password
	 *            user's password.
	 */
	public static RedmineManager createWithUserAuth(final String uri, final String login, final String password) {
		return createWithUserAuth(uri, login, password, createDefaultTransportConfig());
	}

	/**
	 * Creates a new redmine managen with user-based authentication.
	 *
	 * @param uri
	 *            redmine manager URI.
	 * @param login
	 *            user's name.
	 * @param password
	 *            user's password.
	 * @param config
	 *            transport configuration.
	 */
	public static RedmineManager createWithUserAuth(final String uri, final String login, final String password,
			final TransportConfiguration config) {
		final ITransport transport = new Transport(new URIConfigurator(uri, null), config.client);
		transport.setCredentials(login, password);
		return new RedmineManager(transport, config.shutdownListener);
	}

	/**
	 * Builds a new transport.
	 *
	 * @param _uri
	 *            the uri
	 * @param _client
	 *            the transport
	 * @return
	 */
	public static ITransport getNewTransport(final String _uri, final CloseableHttpClient _client) {
		return new Transport(new URIConfigurator(_uri, null), _client);
	}

	/**
	 * Builds a new transport.
	 *
	 * @param _redmineHost
	 *            the redmine Host url
	 * @param _apiKEY
	 *            the api key
	 * @param _client
	 *            the transport
	 * @return
	 */
	public static ITransport getNewTransport(final String _redmineHost, final String _apiKEY,
			final CloseableHttpClient _client) {
		return new Transport(new URIConfigurator(_redmineHost, null), _client);
	}

	// private static void configureProxy(final CloseableHttpClient httpclient)
	// {
	// final String proxyHost = System.getProperty("http.proxyHost");
	// final String proxyPort = System.getProperty("http.proxyPort");
	// if (proxyHost != null && proxyPort != null) {
	// int port;
	// try {
	// port = Integer.parseInt(proxyPort);
	// } catch (final NumberFormatException e) {
	// throw new RedmineConfigurationException("Illegal proxy port " +
	// proxyPort, e);
	// }
	// final HttpHost proxy = new HttpHost(proxyHost, port);
	// httpclient.getParams().setParameter(org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY,
	// proxy);
	// final String proxyUser = System.getProperty("http.proxyUser");
	// if (proxyUser != null) {
	// final String proxyPassword = System.getProperty("http.proxyPassword");
	// httpclient.getCredentialsProvider().setCredentials(new
	// AuthScope(proxyHost, port),
	// new UsernamePasswordCredentials(proxyUser, proxyPassword));
	// }
	// }
	// }

	private static TransportConfiguration createDefaultTransportConfig() {
		return TransportConfiguration.create(HttpClientBuilder.create().build(), null);
	}

	/**
	 * Prevent construction of this object even with use of dirty tricks.
	 */
	private RedmineManagerFactory() {
		throw new UnsupportedOperationException();
	}
}
