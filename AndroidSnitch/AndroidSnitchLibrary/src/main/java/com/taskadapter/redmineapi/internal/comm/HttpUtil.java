package com.taskadapter.redmineapi.internal.comm;

import com.taskadapter.redmineapi.RedmineConfigurationException;
import com.taskadapter.redmineapi.internal.comm.naivessl.NaiveSSLSocketFactory;
import com.taskadapter.redmineapi.internal.comm.naivessl.NaiveX509TrustManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

class HttpUtil {
	public static DefaultHttpClient getNewHttpClient(
			ClientConnectionManager connectionManager) {
		try {

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			final DefaultHttpClient result = new DefaultHttpClient(
					connectionManager, params);
			configureProxy(result);
			return result;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	static ClientConnectionManager createConnectionManager(
			int maxConnections) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			KeyManagementException, UnrecoverableKeyException {


    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(null, new TrustManager[]{new NaiveX509TrustManager()}, null);
    SSLSocketFactory factory = new NaiveSSLSocketFactory(ctx);
//        SSLSocketFactory factory = NaiveSSLFactory.createNaiveSSLSocketFactory();

        SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory
        (), 80));
		registry.register(new Scheme("https", factory, 443));

    ClientConnectionManager mgr = new ThreadSafeClientConnManager(new
        BasicHttpParams(), registry);

    return mgr;
//        PoolingClientConnectionManager manager = new PoolingClientConnectionManager(registry);
//		manager.setMaxTotal(maxConnections);
//		manager.setDefaultMaxPerRoute(maxConnections);
//		return manager;
	}

	private static void configureProxy(DefaultHttpClient httpclient) {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null && proxyPort != null) {
			int port;
			try {
				port = Integer.parseInt(proxyPort);
			} catch (NumberFormatException e) {
				throw new RedmineConfigurationException("Illegal proxy port "
						+ proxyPort, e);
			}
			HttpHost proxy = new HttpHost(proxyHost, port);
			httpclient.getParams().setParameter(
					org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY,
					proxy);
			String proxyUser = System.getProperty("http.proxyUser");
			if (proxyUser != null) {
				String proxyPassword = System.getProperty("http.proxyPassword");
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(proxyHost, port),
						new UsernamePasswordCredentials(proxyUser,
								proxyPassword));
			}
		}
	}

	/**
	 * Returns entity encoding.
	 * 
	 * @param entity
	 *            entitity to get encoding.
	 * @return entity encoding string.
	 */
	public static String getEntityEncoding(HttpEntity entity) {
		final Header header = entity.getContentEncoding();
		if (header == null)
			return null;
		return header.getValue();
	}

	/**
	 * Returns entity charset to use.
	 * 
	 * @param entity
	 *            entity to check.
	 * @return entity charset to use in decoding.
	 */
	public static String getCharset(HttpEntity entity) {
		final String guess = EntityUtils.getContentCharSet(entity);
		return guess == null ? HTTP.DEFAULT_CONTENT_CHARSET : guess;
	}
}
