package fnc.sync.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoadConfig { 
	
	final static String ILLEGAL_WINDOWS_CHARS="[:\\\\/*?|<>]";
	
	public LoadConfig()
	{
	}
	
	static public JSONArray makeConfig(JSONArray apps, JSONArray scans) {
		/*TODO: Add an application and scan object to the config file
		 * {
		 * "aseAppName":<app id or name>,
		 * "asocAppId" :<asoc id>,
		 * "asocScans":[
		 * {"scanId":"scan1", "scanType":<scanner id>, "scanName":<scan name>},
		 * {"scanId":"scan2", "scanType":<scanner id>, "scanName":<scan name>},
		 * {"scanId":"scan3", "scanType":<scanner id>, "scanName":<scan name>}
		 * ]
		 * }
		 */
		
		//First create the list for the App2App Sync
		JSONArray asocConfig = new JSONArray();		
		Map appIdScanList = new HashMap();  // Contains a map of the id of a scan and attached scans (id, JSONObject with scan data)
		Map appIdNameList = new HashMap();//A map of the (id , name) of an asoc scan

		List<String> scansToAppList = new ArrayList<String>();
		scansToAppList = readScansToAppList();
		
		for(int i=0;i<apps.length();i++) {
			//Set the "appId" value
			//If the app is not on my scan 2 app list or that list does not exist add it to the main config  
			if(!scansToAppList.contains(apps.getJSONObject(i).getString("Id")))			
			//Create the "scans" value			 
			{
				appIdNameList.put(apps.getJSONObject(i).getString("Id"),apps.getJSONObject(i).getString("Name"));
				//appIdNameList.put("aseAppName",apps.getJSONObject(i).getString("Name"));
				//appIdNameList.put("asocAppId", apps.getJSONObject(i).getString("Id"));
				appIdScanList.put(apps.getJSONObject(i).getString("Id"),new JSONArray());
				//appConfig.put("aseTranslation", Translations.asoc2ase(apps.getJSONObject(i)));
			}	
		}
			
		//Add the scans to the app list
		JSONArray aseScanList = new JSONArray();
		
		System.out.println("Attaching the scans to the app list");
		for(int j=0;j<scans.length();j++) {
			JSONObject aseScan = new JSONObject();
			String scanName = scans.getJSONObject(j).getString("Name").replaceAll(ILLEGAL_WINDOWS_CHARS, "_");
			String attachedToAppId =  scans.getJSONObject(j).getString("AppId");
			
			//If the scan is completed and not previously synced add it to the "to do" list
			if(!scanName.contains("synced-")) {
				if(appIdScanList.containsKey(attachedToAppId)) {
					//JSONObject asocScan = getScanInfo(asocScanList.getJSONObject(j).getString("scanId"), scans);
					aseScan.put("scanId",scans.getJSONObject(j).getString("Id"));
					aseScan.put("scanType",scans.getJSONObject(j).getString("Technology"));
					aseScan.put("scanName", scanName);
					aseScanList.put(aseScan);
					aseScanList = (JSONArray) appIdScanList.get(attachedToAppId);
					aseScanList.put(aseScan);
					appIdScanList.put(attachedToAppId,aseScanList);
					scans.remove(j);j--; //Remove the scans that have already been added to the file
				}
			}
			else {
				scans.remove(j);j--;  //Remove the scans that have already been synced
			}
		}


		//Combine the appIdScanList and appIdNameList into the json config
		for(Object appId : appIdScanList.keySet()) {
			JSONObject asocScan = new JSONObject();
			asocScan.put("aseAppName",((String) appIdNameList.get(appId)).replaceAll(ILLEGAL_WINDOWS_CHARS, "_"));
			asocScan.put("asocAppId",appId.toString());
			asocScan.put("asocScans", appIdScanList.get(appId));
			asocConfig.put(asocScan);
		}	
		//Check the scans to app list exists
		
		System.out.println(asocConfig.toString());
		
		//Create the list for the Scan2App Sync
		for(int j=0;j<scans.length();j++) {
			JSONObject aseScan = new JSONObject();
			String scanName = scans.getJSONObject(j).getString("Name").replaceAll(ILLEGAL_WINDOWS_CHARS, "_");
			String attachedToAppId =  scans.getJSONObject(j).getString("AppId");
			if(scansToAppList.contains(attachedToAppId)
					&& !scanName.contains("synced-")) {
				aseScanList = new JSONArray();
				JSONObject appConfig = new JSONObject();
				//Create the app container
				appConfig.put("aseAppName",scanName);
				appConfig.put("asocAppId", scans.getJSONObject(j).getString("AppId"));

				//JSONObject asocScan = getScanInfo(asocScanList.getJSONObject(j).getString("scanId"), scans);
				aseScan.put("scanId",scans.getJSONObject(j).getString("Id"));
				aseScan.put("scanType",scans.getJSONObject(j).getString("Technology"));
				aseScan.put("scanName", scanName);
				aseScanList.put(aseScan);
				appConfig.put("asocScans", aseScanList);
				asocConfig.put(appConfig);
			}
		}

		return asocConfig;
	}
		
	private static JSONObject getScanInfo(String scanId, JSONArray scans) {
		
		for(int i=0;i<scans.length();i++) {
			if(scans.getJSONObject(i).getString("Id").equals(scanId)) 
				return scans.getJSONObject(i);
		}
		
		return new JSONObject();
	}
		
	public static void writeConfig(JSONArray asocConfig) {
		File f;
		try {
			f = new File("C:\\ProgramData\\IBM\\ASoC2ASE\\config");
			f.mkdirs();
			PrintWriter writer = new PrintWriter("C:\\ProgramData\\IBM\\ASoC2ASE\\config\\config.json");
			writer.println(asocConfig.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static JSONArray readConfig() {
		
		JSONArray config;
		try {
			BufferedReader br = new BufferedReader(new FileReader("C:\\ProgramData\\IBM\\ASoC2ASE\\config\\config.json"));
			String jsonConfig="";
			String line;
			while((line=br.readLine())!=null) 
				jsonConfig+=line;
			
			config = new JSONArray(jsonConfig);
			//System.out.print(config.toString());
			return config;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new JSONArray();
		
	}
	
	
	public static List<String> readScansToAppList() {
		//This method treats all the scan attached to a specific application as an individual scan in ASE 
		//This is very usful when someone wants to track micro-services independently. 
		JSONArray scansToAppJSONArray;
		List<String> scansToAppList = new ArrayList<String>();
		File f = new File("C:\\ProgramData\\IBM\\ASoC2ASE\\config\\scansToAppsList.json");
		try {			
			if(!f.exists()) createEmptyScan2AppList();
			BufferedReader br = new BufferedReader(new FileReader(f));
			String jsonList ="";
			String line;
			while((line=br.readLine())!=null)  
				jsonList+=line;
			
			scansToAppJSONArray = new JSONObject(jsonList).getJSONArray("scanToAppList");
			
			String appId;
			for(int i=0;i<scansToAppJSONArray.length();i++) {
				appId = scansToAppJSONArray.getJSONObject(i).getString("appId");
				scansToAppList.add(appId);
			}
			
			return scansToAppList;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	private static void createEmptyScan2AppList() {
		PrintWriter writer;
		File f = new File("C:\\ProgramData\\IBM\\ASoC2ASE\\config\\scansToAppsList.json");
		if(!f.exists()) try {
			writer = new PrintWriter(f);
			writer.println("{\n	\"scanToAppList\":[\n]\n}");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to create scan to app sync sample list");
			e.printStackTrace();
		}
	}

}
