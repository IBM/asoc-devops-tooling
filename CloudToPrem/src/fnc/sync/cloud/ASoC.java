package fnc.sync.cloud;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fnc.sync.utils.Translations;

public class ASoC {
	
	private String Token="";
	private JSONArray scans;
	private JSONArray apps;
	
	public ASoC() {
		
	}
	
	//Standard post method for ASoC 
	private String postASoC(JSONObject requestBody,String URL) {
		
		HttpClient httpClient = HttpClientBuilder.create().build();	
		
		try {
			HttpPost request = new HttpPost("https://appscan.ibmcloud.com:443/"+URL);
			StringEntity params = new StringEntity(requestBody.toString());
			if(!this.Token.equals("")) request.addHeader("Authorization","Bearer "+Token);
			request.addHeader("content-type","text/json");
			request.setEntity(params);
			
			HttpResponse response = httpClient.execute(request);
			//System.out.print(EntityUtils.toString(response.getEntity()));
			return EntityUtils.toString(response.getEntity());
			
		} catch(Exception e) {
			//TODO: handle failure
			System.out.print("\nFailed executing POST request on " + URL + " with error:\n" + e.getMessage() );
			return "";
		}
	}
	
	//Standard put method for ASoC 
	private String putASoC(JSONObject requestBody,String URL) {
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			//System.out.print(requestBody);
			HttpPut request = new HttpPut("https://appscan.ibmcloud.com:443/"+URL);
			StringEntity params = new StringEntity(requestBody.toString());
			if(!this.Token.equals("")) request.addHeader("Authorization","Bearer "+Token);
			request.addHeader("content-type","text/json");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			return EntityUtils.toString(response.getEntity());

		} catch(Exception e) {
			//TODO: handle failure
			System.out.print("\nFailed executing PUT request on " + URL + " with error:\n" + e.getMessage() );
			return "";
		}
	}
	
	//Standard get method for ASoC
	private String getASoC(String URL) {
		
		HttpClient httpClient = HttpClientBuilder.create().build();	
		
		try {
			HttpGet request = new HttpGet("https://appscan.ibmcloud.com:443"+URL);
			if(!this.Token.equals("")) request.addHeader("Authorization","Bearer "+Token);
			
			HttpResponse response = httpClient.execute(request);
			return EntityUtils.toString(response.getEntity());
			
		}catch (Exception e) {
			//TODO:Handle exception
			System.out.print("\nFailed executing GET request on " + URL + " with error:\n" + e.getMessage() );
			return "";
		}
		
	}
	
	//Loging method for ASoc
	public String login(String apiKey,String apiSecret) {
		
		JSONObject requestBody = new JSONObject();
		JSONObject responseBody;
		
		try {
		requestBody.put("KeyId", apiKey);
		requestBody.put("KeySecret", apiSecret);
		
		String response = this.postASoC(requestBody,"api/V2/Account/ApiKeyLogin");
		
		
		responseBody = new JSONObject(response);
		
		this.Token=responseBody.getString("Token");
		
		return responseBody.getString("Token");
		
		}catch(Exception e) {
			//TODO: handle the exception
			//System.out.println("Username/password jason in wrong format");
			return "{\"Error\": \""+e.getMessage()+"\" \n \"action\":\"login\"}";
		}
	}
	
	public String pullApps() {
		
		String response = this.getASoC("/api/v2/Apps");
		
		try {
		this.apps = new JSONArray(response);
		} catch (Exception e) {
			return e.getMessage();
		}

		return "Apps returned successfully";
	}
	
	public void getScanXML(String scanName, String scanId, String appName) {
		
		File f = new File("C:\\ProgramData\\IBM\\ASoC2ASE\\scans\\"+appName);
		f.mkdirs();
		
		String path = "C:\\ProgramData\\IBM\\ASoC2ASE\\scans\\"+appName+"\\"+scanName+".xml";
		
		
		try{
			PrintWriter writer = new PrintWriter(path);
			String content = getASoC("/api/v2/Scans/"+scanId+"/Report/Xml");
			content = content.replaceAll("[^\\x20-\\x7e]", "");
			writer.write(content);
			writer.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void markAsSynced(String scanId, String scanName) {
		try {
			String URL = "api/V2/Scans?scanId="+scanId;
			JSONObject newName = new JSONObject();
			newName.put("Name", "synced-"+scanName);
			String response = putASoC(newName,URL);
			//System.out.println(response);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void pullScans() {	
		this.scans = new JSONArray(getASoC("/api/v2/Scans"));
		
		//Remove all the failed scans from the list
		for(int i=0;i<this.scans.length();i++) {
			String scanStatus=scans.getJSONObject(i).get("LastSuccessfulExecution").toString();
			if(!scanStatus.equals("null")) {
				scanStatus = scans.getJSONObject(i).getJSONObject("LastSuccessfulExecution").getString("Status");
				if(!scanStatus.equals("Ready")) {
					this.scans.remove(i);
					i--;
				}
			}
			else {
				this.scans.remove(i);
				i--;
			}
			
		}	
		//System.out.print(scans.toString());
	}
	
	public JSONObject getScanDetails(String scanId) {
		JSONObject scanDetails = new JSONObject(getASoC("/api/v2/Scans/"+scanId));
		return scanDetails;
	}
	
	public JSONArray getApps() {
		return this.apps;
	}
	
	public JSONObject getApp(String id) {
		return new JSONObject(getASoC("/api/v2/Apps/"+id));
	}
	
	public JSONArray getScans() {
		return this.scans;
	}
}
