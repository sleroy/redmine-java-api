package com.taskadapter.redmineapi.internal.comm.naivessl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


/**
 * The goal of this trust manager is to do nothing - it will authorize
 * any TSL/SSL secure connection.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class NaiveX509TrustManager implements X509TrustManager {
	
	@Override
	public void checkClientTrusted(final X509Certificate[] certs, final String str) throws CertificateException {
		// Voluntary ignore
	}
	
	@Override
	public void checkServerTrusted(final X509Certificate[] certs, final String str) throws CertificateException {
		// Voluntary ignore
	}
	
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
