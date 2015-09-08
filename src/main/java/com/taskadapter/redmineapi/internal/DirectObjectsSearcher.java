package com.taskadapter.redmineapi.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;

import com.taskadapter.redmineapi.ITransport;
import com.taskadapter.redmineapi.RedmineException;

public final class DirectObjectsSearcher {
	public static <T> List<T> getObjectsListNoPaging(final ITransport transport, final Map<String, String> map, final Class<T> classRef) throws RedmineException {
		final Set<NameValuePair> params = ParameterMapConverter.getNameValuePairs(map);
		final Transport.ResultsWrapper<T> wrapper = transport.getObjectsListNoPaging(classRef, params);
		return wrapper.getResults();
	}
}
