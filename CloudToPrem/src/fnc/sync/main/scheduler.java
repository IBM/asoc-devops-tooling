package fnc.sync.main;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//import org.identityconnectors.common.security;

import org.json.JSONArray;
import org.json.JSONObject;

import fnc.sync.cloud.ASoC;
import fnc.sync.prem.ASE;
import fnc.sync.utils.LoadConfig;
import fnc.sync.utils.Translations;

public class scheduler{
	final static String HELP_FOR_USAGE="Run -help for usage info";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String asePassword;
		String apiSecret;
		JSONObject applications;	
		String aseServer;
		String aseUser;
		String apiKey;
		
		if(args[0].equals("-help") || args.length<9)
			System.out.println("Command line usage is:\n-aseServer https://<server>:9443/ase " + 
					"-aseU <domain>\\<username> " + 
					"-aseP <password> " + 
					"-asocID <apiKey> " + 
					"-asocSecret <apiSecret>  in the exact specific order");
		else {
		
		aseServer = args[1];
		aseUser = args[3];
		asePassword = args[5];
		apiKey = args[7];
		apiSecret = args[9];
		
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(() -> {
			try {
				myTask(aseServer,aseUser,asePassword,apiKey,apiSecret);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 0, 30, TimeUnit.MINUTES);

		}
	}
	
	private static void myTask(String aseServer,String aseUser,String asePassword,String apiKey,String apiSecret) throws InterruptedException {
	    //System.out.println("Running");
		Date date = new Date();
		
		System.out.println("Sync started at "+date);

	    ASoC asoc = new ASoC();
	    asoc.login(apiKey,apiSecret);
	    asoc.pullApps();
	    asoc.pullScans();
	    
	    
	    ASE ase = new ASE();
	    ase.setBaseUrl(aseServer+"/api/");
	    System.out.println(ase.login(aseUser, asePassword));
	    JSONObject scannersMap = Translations.getScannerValues(ase.getScanners());
	    
	    
	    //Get the default ASoC config
	    JSONArray defaultConfig = LoadConfig.makeConfig(asoc.getApps(),asoc.getScans());
	    //write the config to a file
	    LoadConfig.writeConfig(defaultConfig);
	    
	    
	    //Read the configuration from the disk
	    JSONArray config = LoadConfig.readConfig();
	    
	    //Iterate through all the objects in the config list.
	    for(Object app:config) {
	    	String appName = new JSONObject(app.toString()).getString("aseAppName");
	    	String aseId;
	    	//Check if the app exists in ASE 
	    	if(ase.checkApp(appName)) {
	    		//Get app id details if app exists  
	    		JSONObject aseApp = new JSONArray(ase.getApp(appName)).getJSONObject(0);
	    		aseId = aseApp.getString("id");
	    	}
	    	else {
	    		//Create app if it doesn't exist
	    		//JSONObject aseApp = new JSONObject(ase.addApp(Translations.asoc2ase(asoc.getApp(new JSONObject(app.toString()).getString("asocAppId")))));
	    		JSONObject aseApp = new JSONObject(ase.addApp(Translations.asoc2ase(asoc.getApp(new JSONObject(app.toString()).getString("asocAppId")),appName)));
	    		aseId=aseApp.get("id").toString();
	    	}
	    	JSONArray scans = new JSONObject(app.toString()).getJSONArray("asocScans");
	    		for(Object scan:scans) {
	    			String scanName = new JSONObject(scan.toString()).getString("scanName");
	    			String scanId = new JSONObject(scan.toString()).getString("scanId");
	    			{
	    				asoc.getScanXML(scanName, scanId, appName);
	    				System.out.println("For app \""+appName+ "\" downloaded \"" + scanName + "\" scan data");
	    				TimeUnit.SECONDS.sleep(3);
	    				//Put the scan into ase
	    				ase.addScanResults(scanName, aseId, Translations.getScanType(new JSONObject(scan.toString()).getString("scanType"),scannersMap),"C:\\ProgramData\\IBM\\ASoC2ASE\\scans\\"+appName+"\\"+scanName+".xml");
	    				//Sleep for 3 seconds to allow the item to upload
	    				TimeUnit.SECONDS.sleep(3);
	    				
	    				//Change the name in ASoC to show the item has been pulled into ASE
	    				//asoc.markAsSynced(scanId, scanName);
	    				
	    				//Change ASE app status to in progress if not already done.
	    				if(new JSONArray(ase.getApp(appName)).getJSONObject(0).getString("testingstatus").equals("Not Started")) ase.updateTestStatus(appName);
	    				if(!new JSONArray(ase.getApp(appName)).getJSONObject(0).has("tags")) ase.addAsocTags(appName);
	    				}
	    		}

	    }
	    
	    System.out.println("Sync ended at " + date +"\n");
	    ase.logout();
	    
	    
	}
}
