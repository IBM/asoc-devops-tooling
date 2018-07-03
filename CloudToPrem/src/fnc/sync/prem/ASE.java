package fnc.sync.prem;

import java.io.File;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import fnc.sync.utils.DynamicProxyRoutePlanner;

public class ASE {
	private String baseUrl="";
	private String Token="";
	private HttpClient httpClient=getHttpClient();
	
	public ASE() {
		
	}
	
	//Disable certificate checks.
	static HttpHost proxy = new HttpHost("127.0.0.1", 8888);
	static DynamicProxyRoutePlanner routePlanner= new DynamicProxyRoutePlanner(proxy);

	private static HttpClient getHttpClient()
	{
		SSLContext sslcontext;
		try {
			sslcontext = SSLContexts.custom ().loadTrustMaterial ( 
					    new TrustStrategy ()
					    {
					        public boolean isTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
					            return true;
					        }
					    })
					.build();
			
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory ( 
				    sslcontext, null, null, new NoopHostnameVerifier () 
				);
		//Proxied request 
		//HttpClient httpClient = HttpClients.custom().setRoutePlanner(routePlanner).setSSLSocketFactory(sslsf).build();
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		return httpClient;
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return HttpClients.custom().build();
	}
	
	private String postASE(JSONObject requestBody,String URL) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		//TODO: If you are not using self signed untrusted certificates, use this
		//HttpClient httpClient=getHttpClient();
		
		//TODO: If you are not using self signed certificates use this:
		//HttpClient httpClient = HttpClients.custom().build();
		
		
		try {
			
			HttpPost request = new HttpPost(baseUrl+URL);
			StringEntity params = new StringEntity(requestBody.toString());
			if(!this.Token.equals("")) request.addHeader("asc_xsrf_token",Token);
			request.addHeader("content-type","application/json");
			request.setEntity(params);
			
			HttpResponse response = httpClient.execute(request);
			//System.out.print(EntityUtils.toString(response.getEntity()));
			return EntityUtils.toString(response.getEntity());
			
		} catch(Exception e) {
			//TODO: handle failure
			System.out.println(e.getLocalizedMessage());
			return "{\"Error\": \""+e.getMessage()+"\", \n \"action\":\""+URL+"\"}";
		}
	}

	private String putASE(JSONObject requestBody, String URL) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		try {
			HttpPut request = new HttpPut(baseUrl+URL);
			StringEntity params = new StringEntity(requestBody.toString());
			if(!this.Token.equals("")) request.addHeader("asc_xsrf_token",Token);
			request.addHeader("content-type","application/json");
			request.setEntity(params);
			
			HttpResponse response = httpClient.execute(request);
			return EntityUtils.toString(response.getEntity());
		}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			return "{\"Error\": \""+e.getMessage()+"\", \n \"action\":\""+URL+"\"}";
		}
	}
	
	
	//Method to do get requests against the ASE server
	private String getASE(String URL) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		//TODO: If you are not using self signed untrusted certificates, use this
		//HttpClient httpClient=getHttpClient();
		
		//TODO: If you are not using self signed certificates use this:
		//HttpClient httpClient = HttpClients.custom().build();
		
		try {
			HttpGet request = new HttpGet(baseUrl+URL);
			if(!this.Token.equals("")) request.addHeader("asc_xsrf_token",Token);
			
			HttpResponse response = httpClient.execute(request);
			return EntityUtils.toString(response.getEntity());
			
		}catch (Exception e) {
			//TODO:Handle exception
			return "{\"Error\": \""+e.getMessage()+"\", \n \"action\":\""+URL+"\"}";
		}
		
	}
	
	public void setBaseUrl(String url) {
		this.baseUrl=url;
	}
	
	public String login(String username,String password) {
		
		JSONObject requestBody = new JSONObject();
		JSONObject responseBody;
		
		requestBody.put("userId", username);
		requestBody.put("password", password);
		requestBody.put("featureKey", "AppScanEnterpriseUser");
		
		
		try {
			responseBody=new JSONObject(this.postASE(requestBody, "login"));
			this.Token=responseBody.get("sessionId").toString();
			//System.out.println(this.Token);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		
		return "";
	}
	
	public void logout() {
		try {
			
			System.out.println(getASE("logout"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	//Get a JSON list of all the apps currently in ASE. 
	public String getApps() {
		JSONArray requestBody = new JSONArray();
		try {
			System.out.println(getASE("applications"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return requestBody.toString();
	}
	
	public String getApp(String appName) {
		try {
			return getASE("applications?query="+URLEncoder.encode("name="+appName, "UTF-8"));
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	//Check if an appliacation exists in ASE based on name 
	public boolean checkApp(String name) {
		JSONArray responseBody;
		try {
				responseBody=new JSONArray(getASE("applications?query="+URLEncoder.encode("name="+name, "UTF-8")));
				if(responseBody.length()==0) return false;
				
		} catch (Exception e) {
				System.out.println(e.getMessage());
		}
			
		return true;
	}
	
	public String importStatus() {
		
		try {
			return getASE("/issueimport/currentstatus");
		} catch (Exception e)
		{
			return e.getMessage();
		}
	}
	
	public String addApp(JSONObject app) {
		try {
			return this.postASE(app, "applications");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Failed";
	}
	
	private String setAseAppAttribute(String appName,String attribute, String value) {
		JSONObject aseApp = new JSONArray(getApp(appName)).getJSONObject(0);
		JSONObject testingStatus = new JSONObject();
		JSONObject request = new JSONObject();
		request.put("name", appName);
		request.put("id", Integer.parseInt(aseApp.getString("id")));
		request.put("lastUpdated", Long.parseLong(aseApp.getString("lastupdated_timestamp")));
		request.put("description",aseApp.getString("description"));
		request.put("attributeCollection",testingStatus.append("attributeArray", addAtribute(attribute, value))); 
		
		try {
			String response=putASE(request, "applications/" + aseApp.getString("id"));
			return response;
		}
		catch(Exception e) {
			return e.getMessage();
		}		
	}
	
	public void updateTestStatus(String appName) {
		this.setAseAppAttribute(appName,"Testing Status", "In Progress"); 		
	}
	
	public void addAsocTags(String appName) {
		this.setAseAppAttribute(appName,"Tags" , "asoc"); 
				
	}
	
	public String addScanResults(String scanName, String appId, String scanType,String path) {
		/*TODO: get the scan from the list of scan stored in the temp folder
		 * 
		 * Use: JSONObject app for this
		 * {
		 * "appId":<app id or name>,
		 * "scans":[
		 * {"scanId":"scan1", "scanType":<scanner id>},
		 * {"scanId":"scan2", "scanType":<scanner id>},
		 * {"scanId":"scan3", "scanType":<scanner id>}
		 * ]
		 * }
		 *  3 - SAST, 5 - iOs, 7 - DAST, 8 - Mobile
		*/
		
		System.out.println("Trying to upload data");

		String URL = "issueimport/"+appId+"/"+scanType;
		try {
			
			File f = new File(path);
			//System.out.println(scanFile.toString());
			
			HttpPost request = new HttpPost(baseUrl+URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			String boundary = "---------------"+UUID.randomUUID().toString();
			//boundary="-----------------------------10045276064876";
			builder.setBoundary(boundary);
			
			//create upload file params
			builder.addTextBody("asc_xsrf_token", Token);
			builder.addTextBody("scanName", URLEncoder.encode(scanName,"UTF-8"));
			builder.addBinaryBody("uploadedfile", f);
			HttpEntity params= builder.build();
			request.setEntity(params);
			
			if(!this.Token.equals("")) request.addHeader("asc_xsrf_token",Token);
			request.addHeader("content-type","multipart/form-data;boundary="+boundary);
			
			HttpResponse response = httpClient.execute(request);
			//System.out.println(response.toString());
			
			return EntityUtils.toString(response.getEntity());
		
			
		} catch(Exception e) {
			//TODO: handle failure
			System.out.println(e.getLocalizedMessage());
			return e.toString();
		}
		
	}
	
	public JSONObject getScanners() {
		try {
		return new JSONObject(getASE("/scanners?includeUnregisteredScanners=false"));
		}
		catch (Exception e) {
			return new JSONObject().put("Error", e.getMessage());
		}
	}
	
	private static JSONObject addAtribute(String name, String value) {
		JSONObject attribute= new JSONObject();
		
		attribute.put("name", name);
		attribute.append("value", value);
		
		return attribute;
	}
}
