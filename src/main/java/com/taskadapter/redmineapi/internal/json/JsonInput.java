package com.taskadapter.redmineapi.internal.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonInput {
	/**
	 * Returns a json array as "not-null" value.
	 *
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	public static JSONArray getArrayNotNull(final JSONObject obj, final String field)
			throws JSONException {
		return obj.getJSONArray(field);
	}

	/**
	 * Returns a json array as "not-null" value.
	 *
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	public static JSONArray getArrayOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return obj.getJSONArray(field);
	}

	/**
	 * Fetch a date or null.
	 *
	 * @param obj
	 *            object to get.
	 * @param field
	 *            field to use.
	 * @param dateFormat
	 *            field date format.
	 * @return data format.
	 * @throws JSONException
	 *             if error occurs.
	 */
	public static Date getDateOrNull(final JSONObject obj, final String field,
			final SimpleDateFormat dateFormat) throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		final String guess = obj.getString(field);
		try {
			return dateFormat.parse(guess);
		} catch (final ParseException e) {
			throw new JSONException("Bad date value " + guess + " " + e.getMessage());
		}
	}

	/**
	 * Fetches an optional float from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static Float getFloatOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return (float) obj.getDouble(field);
	}

	/**
	 * Fetches an int from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static int getInt(final JSONObject obj, final String field) throws JSONException {
		return obj.getInt(field);
	}
	
	/**
	 * Fetches an int from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @param deflt
	 *            default value.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static int getInt(final JSONObject obj, final String field, final int deflt)
			throws JSONException {
		return obj.optInt(field, deflt);
	}

	/**
	 * Fetches an optional int from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static Integer getIntOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return obj.getInt(field);
	}

	/**
	 * Parses required item list.
	 *
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws JSONException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListNotNull(final JSONObject obj, final String field,
			final JsonObjectParser<T> parser) throws JSONException {
		final JSONArray items = getArrayNotNull(obj, field);
		final int length = items.length();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++) {
			result.add(parser.parse(items.getJSONObject(i)));
		}
		return result;
	}

	/**
	 * Parses optional item list.
	 *
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws JSONException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListOrEmpty(final JSONObject obj, final String field,
			final JsonObjectParser<T> parser) throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return new ArrayList<T>();
		}
		final JSONArray items = obj.getJSONArray(field);
		if (items == null) {
			return new ArrayList<T>();
		}
		final int length = items.length();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++) {
			result.add(parser.parse(items.getJSONObject(i)));
		}
		return result;
	}

	/**
	 * Parses optional item list.
	 *
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws JSONException
	 *             if format is invalid.
	 */
	public static <T> List<T> getListOrNull(final JSONObject obj, final String field,
			final JsonObjectParser<T> parser) throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		final JSONArray items = obj.getJSONArray(field);
		final int length = items.length();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++) {
			result.add(parser.parse(items.getJSONObject(i)));
		}
		return result;
	}

	/**
	 * Fetches a long from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static long getLong(final JSONObject obj, final String field)
			throws JSONException {
		return obj.getLong(field);
	}

	/**
	 * Fetches an optional long from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static Long getLongOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return obj.getLong(field);
	}

	/**
	 * Returns a json object field for a specified object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws JSONException
	 *             if target field is not an object.
	 */
	public static JSONObject getObjectNotNull(final JSONObject obj, final String field)
			throws JSONException {
		return obj.getJSONObject(field);
	}

	/**
	 * Returns a json object field for a specified object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws JSONException
	 *             if target field is not an object.
	 */
	public static JSONObject getObjectOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return obj.getJSONObject(field);
	}

	/**
	 * Retreive optional object.
	 *
	 * @param obj
	 *            object to parse.
	 * @param field
	 *            field part.
	 * @param parser
	 *            parset ojbect.
	 * @return parsed object.
	 * @throws JSONException
	 *             if value is not valid.
	 */
	public static <T> T getObjectOrNull(final JSONObject obj, final String field,
			final JsonObjectParser<T> parser) throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return parser.parse(obj.getJSONObject(field));
	}

	/**
	 * Returns an optional "boolean" field value. If field is absent or set to
	 * <code>null</code>, this method returns <code>false</code>.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value for.
	 * @return boolean value.
	 * @throws JSONException
	 *             if input is not valid (field value is not boolean).
	 */
	public static boolean getOptionalBool(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return false;
		}
		return obj.getBoolean(field);
	}

	/**
	 * Fetches a string from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid, not exists, etc...
	 */
	public static String getStringNotNull(final JSONObject obj, final String field)
			throws JSONException {
		return obj.getString(field);
	}

	/**
	 * Fetches an optional string from an object. Absent value is returned as an
	 * empty string instead of null.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid
	 */
	public static String getStringOrEmpty(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return "";
		}
		return obj.getString(field);
	}

	/**
	 * Fetches an optional string from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws JSONException
	 *             if value is not valid
	 */
	public static String getStringOrNull(final JSONObject obj, final String field)
			throws JSONException {
		if (!obj.has(field) || obj.isNull(field)) {
			return null;
		}
		return obj.getString(field);
	}
}
