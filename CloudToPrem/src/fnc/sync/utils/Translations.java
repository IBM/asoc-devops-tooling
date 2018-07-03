package fnc.sync.utils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;


public class Translations {
	private Map<String, String> attributes = new HashMap<String, String>();
	
	Translations() {
		attributes.put("businessimpact","Business Impact");
		attributes.put("testingstatus", "Testing Status");
		attributes.put("businessunit", "Business Unit");
	}
	
	public String translateAppAtr(String attribute) {
		return attributes.get(attribute);
	}
	
	static public JSONObject asoc2ase(JSONObject asocApp) {
		
		JSONObject aseApp = new JSONObject();
		
		JSONObject  aseAttributes = new JSONObject();
		
		aseApp.put("name",  asocApp.get("Name").toString());
		aseApp.put("description", "Local replica for ASoC application "+asocApp.get("Name").toString()+". To see the full details connect to asoc and check application " +asocApp.get("Name").toString());
		
		//Add some attributes 
		aseAttributes.append("attributeArray", addAtribute("Business Unit", asocApp.get("BusinessUnit").toString()));
		String businessImpact =  asocApp.get("BusinessImpact").toString();
		
		if(!businessImpact.equals("Unspecified")) businessImpact += " Impact";
		aseAttributes.append("attributeArray", addAtribute("Business Impact",businessImpact));
		
		String testingStatus = asocApp.get("TestingStatus").toString();
		if(testingStatus.equals("InProgress"))
			testingStatus = "In Progress";
		else if(testingStatus.equals("NotStarted"))
				testingStatus = "Not Started";
		aseAttributes.append("attributeArray", addAtribute("Testing Status", testingStatus));
		aseAttributes.append("attributeArray", addAtribute("Tags", "asoc"));
		aseApp.put("attributeCollection",aseAttributes);
		
		return aseApp;
	}

	static public JSONObject asoc2ase(JSONObject asocApp, String aseName) {
		
		JSONObject aseApp = new JSONObject();
		
		JSONObject  aseAttributes = new JSONObject();
		
		aseApp.put("name", aseName);
		aseApp.put("description", "Local replica for ASoC application "+asocApp.get("Name").toString()+". To see the full details connect to asoc and check application " +asocApp.get("Name").toString());
		
		//Add some attributes 
		aseAttributes.append("attributeArray", addAtribute("Business Unit", asocApp.get("BusinessUnit").toString()));
		String businessImpact =  asocApp.get("BusinessImpact").toString();
		
		if(!businessImpact.equals("Unspecified")) businessImpact += " Impact";
		aseAttributes.append("attributeArray", addAtribute("Business Impact",businessImpact));
		
		String testingStatus = asocApp.get("TestingStatus").toString();
		if(testingStatus.equals("InProgress"))
			testingStatus = "In Progress";
		else if(testingStatus.equals("NotStarted"))
				testingStatus = "Not Started";
		aseAttributes.append("attributeArray", addAtribute("Testing Status", testingStatus));
		aseAttributes.append("attributeArray", addAtribute("Tags", "asoc"));
		aseApp.put("attributeCollection",aseAttributes);
		
		return aseApp;
	}
	
	private static JSONObject addAtribute(String name, String value) {
		JSONObject attribute= new JSONObject();
		
		attribute.put("name", name);
		attribute.append("value", value);
		
		return attribute;
	}
	
	static public JSONObject getScannerValues(JSONObject scannersList) {
		
		JSONObject scannersMap = new JSONObject();
		JSONArray scanners = scannersList.getJSONArray("scannerColl");
		//System.out.println("Looking for scanners");
		for(int i=0;i<scanners.length();i++) 
		{
			JSONObject scannerJson = scanners.getJSONObject(i);
			//System.out.println("Looking for scanner " + scannerJson.getString("name"));

			switch (scannerJson.getString("name")) {
					case "AppScan Mobile Analyzer for iOS":
						scannersMap.put("iOs",scannerJson.get("id").toString());
						break;
						
					case "AppScan Mobile Analyzer":
						scannersMap.put("Android",scannerJson.get("id").toString());
						break;

					case "AppScan Static Analyzer":
						scannersMap.put("SAST",scannerJson.get("id").toString());
						break;
			
					case "AppScan Dynamic Analyzer":
						scannersMap.put("DAST",scannerJson.get("id").toString());
						break;
					
					default:
						break;
			}
		}
		
		return scannersMap;
	}
		
	static public String getScanType(String scanType,JSONObject scannerMap) {
		/*  Scanner ids:
		 *  	3 - ASoC Static Analyser 
		 *  	5 - ASoC Mobile Analyser iOs 
		 *  	7 - ASoC DAST
		 *  	8 - ASoC Mobile Analyser  
		*/
		
		if(scanType.equals("StaticAnalyzer") || scanType.equals("IFA"))  return scannerMap.getString("SAST");
		if(scanType.equals("DynamicAnalyzer"))return scannerMap.getString("DAST");
		if(scanType.equals("MobileAnalyzerAndroid")) return scannerMap.getString("Android");
		return  scannerMap.getString("iOs");

	}
	
}
