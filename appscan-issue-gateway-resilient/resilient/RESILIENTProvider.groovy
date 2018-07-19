/**
 * � Copyright Cody Travis
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package resilient

import common.IAppScanIssue
import common.IProvider
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import resilient.ASoCWrapper

class RESILIENTProvider extends RESILIENTConstants implements IProvider {
	
	@Override
	public String getId() {
		return PROVIDER_NAME
	}

	@Override
	List<String> getDescription() {
		return PROVIDER_DESCRIPTION
	}

	@Override
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {
		
		int retCode = 0
		if(issues.length == 0) {
			results.put("Result","Success")
			results.put("Message","Criteria for incident creation not met")
		} else {
			println("Incident Creation Conditions Met - Creating Incident")
			println("Number of issues: " + issues.length)
			retCode = createIncident(issues, config, results, errors)
		}
	}
	
	private Boolean validate(Map<String, String> config, List<String> errors) {
		
		//Check for required fields
		boolean valid = true		
		return valid
	}
	
	//For now just remove any double quotes.  Causes problems
	private String escape(String theString) {
		theString.replaceAll("\"", "'")
	}
	
	private int createIncident(IAppScanIssue[] issues, Map<String, Object> config, Map<String, String> results, List<String> errors){
		//return codes 0=No Errors
		def ret = 0
		def appId = config.get("app_id")
		def asocApiKeyId = config.get("asoc_key_id")
		def asocApiKeySec = config.get("asoc_key_secret")
		def asocUrl = config.get("asoc_url")
		
		//Create the ASoC API Wrapper and login
		def asoc = new ASoCWrapper(asocApiKeyId, asocApiKeySec, asocUrl)
		if(!asoc.login()){
			println("ASoC Login Failed")
			results.put("Result","Error")
			errors.put("ASoC Login Failed")
			return -1
		}
		println("Logged into ASoC")
		
		//Find the App Name associated with that App ID
		println("Retrieving application details for appId: " + appId)
		
		def app = asoc.getAppInfo(appId)
		
		if(app == null){
			println("ASoC Application not found")
			results.put("Result","Error")
			errors.put("ASoC Application Name not found")
			return -1
		}
		
		
		def incidentName = "Security Flaws Found In Application: " + app.Name
		def incidentDesc = "IBM Application Security on Cloud discovered vulnerabilities " +
			"in an application critical to the business.<p><br>" +
			"<span style='font-weight: bold'>Vulnerable Application:&nbsp</span>"+ asoc.htmlEncode(app.Name) +"<br>" +
			"<span style='font-weight: bold'>Risk Rating:&nbsp</span>"+ asoc.htmlEncode(app.RiskRating) +"<br>" +
			"<span style='font-weight: bold'>Business Impact:&nbsp</span>"+ asoc.htmlEncode(app.BusinessImpact) +"<br>" +
			"<span style='font-weight: bold'>Collateral Damage Potential:&nbsp</span>"+ asoc.htmlEncode(app.CollateralDamagePotential) +"<br>" +
			"<span style='font-weight: bold'>BusinessUnit:&nbsp</span>"+ asoc.htmlEncode(app.BusinessUnit) +"<br>" +
			"<span style='font-weight: bold'>URL:&nbsp</span>"+ asoc.htmlEncode(app.Url) +"<br>" +
			"<span style='font-weight: bold'>Hosts:&nbsp</span>"+ asoc.htmlEncode(app.Hosts) +"<br>" +
			"<span style='font-weight: bold'>Business Owner:&nbsp</span>"+ asoc.htmlEncode(app.BusinessOwner) +"<br>" +
			"<span style='font-weight: bold'>Development Contact:&nbsp</span>"+ asoc.htmlEncode(app.DevelopmentContact) +"" +
			"<p><span style='font-weight: bold'>Issue Summary</span><br>"+
			"High Issues:&nbsp"+ app.HighIssues +"<br>" +
			"Medium Issues:&nbsp"+ app.MediumIssues +"<br>" +
			"Low Issues:&nbsp"+ app.LowIssues +"" +
			"<p>See attached report for full details"
			
		//Create the PDF report for that App that will be attached to the Resilient Incident
		def reportFileName = app.Name.replaceAll("[^A-Za-z0-9]", "_")
		println("Downloading Issues Report")
		def reportPath = asoc.getAppIssueReport(appId, "Pdf", reportFileName+".pdf")
		if(reportPath==null){
			println("Error Generating Report")
			results.put("Result","Error")
			errors.put("Error Generating ASoC Application Issue Report")
			return -1
		}
		
		println("Report downloaded")
		
		
		println("Attempting to create incident:" + incidentName)
		def scriptDirPath = new File(getClass().protectionDomain.codeSource.location.path).parent
		def resultStr = ""
		def env = ["APP_CONFIG_FILE="+scriptDirPath+"\\app.config"]
		def command = "python " + scriptDirPath + "\\create_incident.py -n \"" + incidentName + "\" -d \"" + incidentDesc + "\" "
		command += "-t " + "\"Vulnerable Application\" -a \"" + reportPath + "\""
		def proc = command.execute(env, new File(scriptDirPath))
		def outputStream = new StringBuffer()
		proc.waitForProcessOutput(outputStream, System.err)
		ret = proc.exitValue()
		
		def reportFile = new File(reportPath)
		if(reportFile.exists() && reportFile.delete()){
			println("Deleted the report from the file system")
		}else{
			println("Couldn't delete the report from the file system")
		}
		
		if(ret==0){
			def jsonSlurper = new JsonSlurper()
			def jsonObj = jsonSlurper.parseText(outputStream .toString())
			
			results.put("Result","Success")
			
			results.put("IncidentId", "\""+jsonObj.inc_id+"\"")
			results.put("IncidentName", jsonObj.inc_name)
			results.put("Attachment", jsonObj.name)
			
			println("Created Incident: " + jsonObj.inc_id)
			println("Done")
		
			return ret
		}
		//error cases
		switch(ret){
			case 1:
				println("Incident already exists - no action taken")
				results.put("Result","Success")
				results.put("Message","Incident already exists - no action taken")
				return 0
		}
		
		
	}
}
