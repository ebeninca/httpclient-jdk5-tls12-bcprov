package com.example.HttpsMy;

import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

public class HttpsClient {

	public static void main(String args[]) throws Exception {

		System.setProperty("javax.net.debug", "ssl:handshake");
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);

		HttpPost httpPost = new HttpPost(
				"https://sei.fazenda.gov.br/sei/ws/SeiWS.php");
		httpPost.addHeader("content-type", "text/xml");

		StringEntity lEntity = new StringEntity(getRequest().toString());
		httpPost.setEntity(lEntity);

		HttpClient client = HttpClientBuilder.create()
				.setSSLSocketFactory(getFactory()).build();

		HttpResponse resp = client.execute(httpPost);

		System.out.println(resp.getStatusLine());
		System.out.println(resp.getStatusLine().getReasonPhrase());
		System.out.println(EntityUtils.toString(resp.getEntity()));
	}

	private static StringBuffer getRequest() {

		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		buffer.append(
				"<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		buffer.append(
				" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		buffer.append("<soap:Body>");
		buffer.append(
				"<GetCitiesByCountry xmlns=\"http://www.webserviceX.NET\">");
		buffer.append("<CountryName>Brazil</CountryName>");
		buffer.append("</GetCitiesByCountry>");
		buffer.append("</soap:Body>");
		buffer.append("</soap:Envelope>");

		return buffer;
	}

	static SSLContext getContext() throws Exception {

		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] arg0,
							String arg1) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						if (chain == null || chain.length < 1
								|| authType == null || authType.length() < 1) {
							throw new IllegalArgumentException();
						}
						System.out.println(
								"Auto-trusted server certificate chain for: "
										+ chain[0].getSubjectX500Principal()
												.getName());
					}

					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				} };

		SSLContext context = SSLContext.getInstance("TLSv1.2",
				BouncyCastleJsseProvider.PROVIDER_NAME);

		context.init(null, trustAllCerts, SecureRandom.getInstance("DEFAULT",
				BouncyCastleProvider.PROVIDER_NAME));

		return context;
	}

	static SSLConnectionSocketFactory getFactory() throws Exception {
		return new SSLConnectionSocketFactory(getContext(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	}
}