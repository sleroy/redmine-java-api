package com.taskadapter.redmineapi.internal.comm;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineTransportException;

public class BaseCommunicator implements Communicator<HttpResponse>, Closeable {
	private final Logger logger = LoggerFactory.getLogger(BaseCommunicator.class);
	
	private final CloseableHttpClient client;
	
	public BaseCommunicator(final CloseableHttpClient client) {
		this.client = client;
	}
	
	// TODO lots of usages process 404 code themselves, but some don't.
	// check if we can process 404 code in this method instead of forcing
	// clients to deal with it.
	
	@Override
	public void close() throws IOException {
		client.close();
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.taskadapter.redmineapi.internal.comm.Communicator#sendRequest(org.apache.http
	 * .HttpRequest)
	 */
	@Override
	public <R> R sendRequest(final HttpRequest request,
			final ContentHandler<HttpResponse, R> handler) throws RedmineException {
		logger.debug(request.getRequestLine().toString());
		
		request.addHeader("Accept-Encoding", "gzip");
		final HttpClient httpclient = client;
		try {
			final HttpResponse httpResponse = httpclient
					.execute((HttpUriRequest) request);
			try {
				return handler.processContent(httpResponse);
			} finally {
				EntityUtils.consume(httpResponse.getEntity());
				
			}
		} catch (final ClientProtocolException e1) {
			throw new RedmineFormatException(e1);
		} catch (final IOException e1) {
			throw new RedmineTransportException("Cannot fetch data from "
					+ getMessageURI(request) + " : "
					+ e1.toString(), e1);
		}
	}
	
	private String getMessageURI(final HttpRequest request) {
		final String uri = request.getRequestLine().getUri();
		final int paramsIndex = uri.indexOf('?');
		if (paramsIndex >= 0) {
			return uri.substring(0, paramsIndex);
		}
		return uri;
	}
}
