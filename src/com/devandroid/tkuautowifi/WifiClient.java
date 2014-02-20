/*
 * Copyright (C) 2013  Sparks Lab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devandroid.tkuautowifi;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

import com.devandroid.tkuautowifi.callback.LoginCallback;
import com.devandroid.tkuautowifi.callback.LogoutCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class WifiClient {
	private static AsyncHttpClient mClient = Init();

	private static AsyncHttpClient Init() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(90000);
		return client;
	}

	static final int CONNECTION_TIMEOUT = 90000;
	static final int SOCKET_TIMEOUT = 90000;
	static final int RETRY_COUNT = 4;

	private String mUsername;
	private String mPassword;
	private DefaultHttpClient mHttpClient;

	private Context context;

	public WifiClient(String username, String password, Context context) {
		mUsername = username;
		mPassword = password;
		this.context = context;

		HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

		mHttpClient = new DefaultHttpClient(params);
		mHttpClient.setParams(params);

		// mHttpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler()
		// {
		// @Override
		// public boolean retryRequest(IOException exception,
		// int executionCount, HttpContext context) {
		// if (executionCount >= RETRY_COUNT) {
		// return false;
		// }
		// if (exception instanceof UnknownHostException) {
		// return false;
		// }
		// if (exception instanceof ConnectException) {
		// return false;
		// }
		// if (exception instanceof SSLHandshakeException) {
		// return false;
		// }
		//
		// return true;
		// }
		// });

	}

	public static void login(final Context context, String username,
			String password, final LoginCallback callback) {
		final String url = "http://163.13.8.254/cgi-bin/ace_web_auth.cgi";

		String payload = String.format("username=%s&userpwd=%s", username,
				password);
		payload += "&login=%E7%99%BB%E5%85%A5";

		ByteArrayEntity entity = null;
		try {
			entity = new ByteArrayEntity(payload.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		mClient.post(context, url, entity, "application/x-www-form-urlencoded",
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);

						if (statusCode == 200) {
							// if login success, the response contents should
							// contain these words.
							if (content.contains("login_online_detail.php")) {
								if (callback != null) {
									callback.onSuccess();
								}
							} else {
								if (callback != null) {
									callback.onFailure(context
											.getString(R.string.login_error));
								}
							}
						} else {
							if (callback != null) {
								callback.onFailure(context
										.getString(R.string.login_timeout));
							}
						}
					}

					@Override
					public void onFailure(Throwable error, String content) {
						super.onFailure(error, content);

						if (callback != null) {
							callback.onFailure(context
									.getString(R.string.login_timeout));
						}
					}
				});
	}

	// public void login() throws IOException, LoginException {
	// final String url = "http://163.13.8.254/cgi-bin/ace_web_auth.cgi";
	//
	// try {
	// UrlEncodedFormEntity mUrlEncodedFormEntity;
	// HttpPost mHttpPost = new HttpPost(url);
	//
	// ArrayList<BasicNameValuePair> mArrayList = new
	// ArrayList<BasicNameValuePair>();
	// mArrayList.add(new BasicNameValuePair("username", mUsername));
	// mArrayList.add(new BasicNameValuePair("userpwd", mPassword));
	// mArrayList
	// .add(new BasicNameValuePair("login", "%E7%99%BB%E5%85%A5"));
	// mUrlEncodedFormEntity = new UrlEncodedFormEntity(mArrayList,
	// HTTP.UTF_8);
	//
	// mHttpPost.setEntity(mUrlEncodedFormEntity);
	//
	// // it's hacky here, if we try to send request after connecting to
	// // tku immediately, then we'll get a timeout error.
	// // so we sleep for one second, then send the request.
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// HttpResponse httpResponse = mHttpClient.execute(mHttpPost);
	//
	// if (httpResponse.getStatusLine().getStatusCode() == 200) {
	// byte[] byteresponse = EntityUtils.toByteArray(httpResponse
	// .getEntity());
	// String str = EncodingUtils.getString(byteresponse, "big5");
	//
	// Log.i(Constant.TAG, "---Login page---\n" + str.toString());
	//
	// if (!str.contains("login_online_detail.php")) {
	// throw new LoginException("ERROR");
	// }
	// }
	// } catch (SocketTimeoutException e) {
	// throw new SocketTimeoutException();
	// } catch (ProtocolException e) {
	// throw new LoginException("ERROR");
	// }
	// }

	// public void logout() throws IOException, LogoutRepeatException {
	// final String url = "http://163.13.8.254/cgi-bin/ace_web_auth.cgi?logout";
	//
	// HttpGet request = new HttpGet(url);
	// try {
	// HttpResponse response = mHttpClient.execute(request);
	// if (response.getStatusLine().getStatusCode() == 200) {
	// byte[] byteresponse = EntityUtils.toByteArray(response
	// .getEntity());
	// String str = EncodingUtils.getString(byteresponse, "big5");
	//
	// // Log.i(TAG, "---Logout page---\n" + str.toString());
	//
	// if (!str.contains("window.location=\"/login.php\"")) {
	// throw new LogoutRepeatException("LOGOUT_REPEAT");
	// }
	// }
	// } catch (SocketTimeoutException e) {
	// throw new SocketTimeoutException();
	// }
	// }

	public static void logout(final Context context,
			final LogoutCallback callback) {
		final String url = "http://163.13.8.254/cgi-bin/ace_web_auth.cgi?logout";
		mClient.get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				if (statusCode == 200) {
					if (content.contains("login.php")) {
						if (callback != null) {
							callback.onSuccess();
						}
					} else {
						if (callback != null) {
							callback.onFailure(context
									.getString(R.string.logout_error));
						}
					}
				} else {
					if (callback != null) {
						callback.onFailure(context
								.getString(R.string.logout_error));
					}
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);

				if (callback != null) {
					callback.onFailure(context.getString(R.string.logout_error));
				}
			}
		});
	}

	// public String getChangelog() {
	// String changelog = "";
	//
	// String uriAPI =
	// "http://dl.dropbox.com/u/19246876/tku_wifi_changelog.txt";
	// HttpGet request = new HttpGet(uriAPI);
	//
	// HttpParams params = new BasicHttpParams();
	// // HttpConnectionParams.setConnectionTimeout(params,
	// // CONNECTION_TIMEOUT);
	// // HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
	//
	// // request.addHeader("Content-Type",
	// // "application/x-www-form-urlencoded");
	// // request.addHeader("Connection", "keep-alive");
	// // request.addHeader("User-Agent", "Mozilla/5.0");
	//
	// try {
	// HttpResponse response = new DefaultHttpClient(params)
	// .execute(request);
	// if (response.getStatusLine().getStatusCode() == 200) {
	// InputStream in = response.getEntity().getContent();
	// BufferedReader br = new BufferedReader(
	// new InputStreamReader(in));
	// String line = null;
	// int latest_version = 0;
	//
	// while ((line = br.readLine()) != null) {
	// if (line.startsWith("VersionCode:")) {
	// latest_version = Integer.parseInt(line.substring(line
	// .indexOf("VersionCode:") + 12));
	// } else if (line.startsWith("ChangeLog:")) {
	// changelog = line
	// .substring(line.indexOf("ChangeLog:") + 10);
	// }
	// }
	//
	// // Log.i("David", "latest_version: " + latest_version
	// // + " changelog: " + changelog);
	//
	// if (Utils.getVersionCode(context) >= latest_version) {
	// changelog = "";
	// }
	// }
	//
	// } catch (ConnectTimeoutException e) {
	// e.printStackTrace();
	// changelog = "";
	// } catch (SocketTimeoutException e) {
	// e.printStackTrace();
	// changelog = "";
	// } catch (ProtocolException e) {
	// e.printStackTrace();
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return changelog;
	// }

	// @SuppressWarnings("unused")
	// private StringBuilder inputStreamToString(InputStream is)
	// throws IOException {
	// String line = "";
	// StringBuilder total = new StringBuilder();
	// BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	// while ((line = rd.readLine()) != null) {
	// total.append(line);
	// }
	// return total;
	// }

	// public boolean loginRequired() throws IOException {
	// try {
	// HttpClient client = new DefaultHttpClient();
	// HttpGet request = new HttpGet("http://74.125.71.147/"); // google
	// HttpResponse response = client.execute(request);
	//
	// if (response.getStatusLine().getStatusCode() != 200) {
	// return true;
	// }
	//
	// } catch (Exception e) {
	// return true;
	// }
	// return false;
	// }
}
