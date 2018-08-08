/**
 * Author: Cody Travis
 * Date: 2018-04-16
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
 
package resilient

/*
 * Wrapper class for the ASoC API
 */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput 

class ASoCWrapper {
	
	def baseUrl = ""
	def bearerToken = null
	def apiKeyId = ""
	def apiKeySecret = ""
	
	ASoCWrapper(apiKeyId, apiKeySecret, baseUrl="https://appscan.ibmcloud.com"){
		this.apiKeyId = apiKeyId
		this.apiKeySecret = apiKeySecret
		this.baseUrl = baseUrl
	}
	
	public boolean changeIssueStatus(asocAppId, issuesList, newStatus="Open"){
		def headers = [:]
		headers["Authorization"] = "Bearer " + bearerToken
		headers["Content-Type"] = "application/json"
		def json = JsonOutput.toJson(issuesList)
		def response = request(baseUrl+"/api/v2/Apps/"+ asocAppId +"/Issues/Triage?issueStatus=" + newStatus, "PUT", headers, json)
		switch(response.code){
			case 401: 
				println("Error setting issue status: 401 - You are not logged into ASoC")
				return false
			case 403: 
				println("Error setting issue status: 403 - Session Expired?")
				return false
			case 200:
				return true
		}
		
		return false
	}
	
	public Map<String, String> getAppInfo(asocAppId){
		def headers = [:]

		headers["Authorization"] = "Bearer " + bearerToken

		def response = request(baseUrl+"/api/V2/Apps", "GET", headers)
		switch(response.code){
			case 401: 
				println("Error getting App Name: 401 - You are not logged into ASoC")
				return null
			case 403: 
				println("Error getting App Name: 403 - Session Expired?")
				return null
		}
		
		def jsonSlurper = new JsonSlurper()
		def jsonObj = jsonSlurper.parseText(response.body)

		for(app in jsonObj){
			if(app.Id.equals(asocAppId)){
				return app
			}
		}
		println("Application with Id " + asocAppId + " not found")
		return null
	}
	
	public String getAppIssueReport(String appId, reportFormat="Pdf", fileName="Security_Report"){
		def reportLocalPath = ""
		
		//Step 1: Create Report Job and return the JobId
		def jobId = createReportJob(appId, reportFormat)
		
		//Step 2 poll the job until status == ready
		def ready = false
		def status = "Pending"
		def maxWaitSeconds = 600 //5 mins
		def waitSeconds = 0
		while(!status.equals("Ready") || waitSeconds >= maxWaitSeconds){
			status = reportJobStatus(jobId)
			sleep(10*1000)
			waitSeconds += 10
		}
		
		if(waitSeconds >= maxWaitSeconds){
			println("Timeout reached waiting on report generation")
			return null
		}
		
		//Step 3 Download the report and return the file path
		reportLocalPath = downloadReport(jobId, fileName)
		return reportLocalPath
	}
	
	private Map<String, Map<String, String>> getDefaultReportJobModel(){
		Map<String, Map<String, String>> model = [:]
		
		model = [:]
		model["OdataFilter"] = ""
		model["Locale"] = "us"
		model["PolicyIds"] = []
		model["Configuration"] = [:]
		model["Configuration"]["Summary"] = true
		model["Configuration"]["Details"] = true
		model["Configuration"]["Discussion"] = true
		model["Configuration"]["Overview"] = true
		model["Configuration"]["TableOfContent"] = true
		model["Configuration"]["Advisories"] = true
		model["Configuration"]["FixRecommendation"] = true
		model["Configuration"]["History"] = false
		model["Configuration"]["Title"] = ""
		model["Configuration"]["Notes"] = ""
		model["Configuration"]["IsTrialReport"] = false
		model["Configuration"]["RegulationReportType"] = "None"
		model["Configuration"]["ReportFileType"] = "Pdf"
		
		return model
	}
	
	public String createReportJob(appId, fileType="Pdf", reportConfig=null){
		//fileType must be one of ['Pdf', 'Html', 'Xml']
		//RegulationReportType must be one of ['None', 'OwaspTop10_2017', 'Sans25', 'EuGdpr_2016', 'PCI'],
		def headers = [:]
		headers["Authorization"] = "Bearer " + bearerToken
		headers["Content-Type"] = "application/json"
		def config
		if(reportConfig==null){
			config = getDefaultReportJobModel()
		}else{
			config = reportConfig
		}
		config["Configuration"]["ReportFileType"] = fileType
		def json = JsonOutput.toJson(config)
		println(JsonOutput.prettyPrint(json))
		def createReportUrl = "/api/v2/Apps/"+ appId +"/Issues/CreateReport"
		println(createReportUrl)
		def response = request(baseUrl+createReportUrl, "POST", headers, json)
		
		if(response.code != 200){
			println("Error creating report job, status " + response.code)
			return null
		}
		response.print()
		def jsonSlurper = new JsonSlurper()
		def jsonObj = jsonSlurper.parseText(response.body)
		
		def jobId = jsonObj.Id
		
		return jobId
	}	
	
	public String reportJobStatus(jobId){
		
		def headers = [:]
		headers["Authorization"] = "Bearer " + bearerToken
		def response = request(baseUrl+"/api/V2/Issues/ReportJobs/"+jobId, "GET", headers)
		try{
			assert response.code == 200 : "Error getting report job status for job " + jobId
		} catch(AssertionError e){
			println("Response Code: " + response.code + " - " + e.getMessage)
			return "Failed"
		}
		
		//response.print()
		def jsonSlurper = new JsonSlurper()
		def jsonObj = jsonSlurper.parseText(response.body)
		
		def status = jsonObj.Status
		
		return status
	}
	
	public String downloadReport(jobId, fileName){
		def reportLocalPath = ""
		
		def headers = [:]
		headers["Authorization"] = "Bearer " + bearerToken
		def response = request(baseUrl+"/api/V2/Issues/Reports/"+jobId, "GET", headers)
		try{
			assert response.code == 200 : "Error getting report job status for job " + jobId
		} catch(AssertionError e){
			println("Response Code: " + response.code + " - " + e.getMessage)
			return "Failed"
		}
		
		def subdir = new File("reports")
		if(!subdir.exists()){
			subdir.mkdir()
		}
		
		
		File report = new File("reports/"+fileName)
		int i = 0
		while(report.exists()){
			report = new File("reports/"+i+"_"+fileName)
			i++
		}
		
		report.setBytes(response.body)
		reportLocalPath = report.getAbsolutePath()
		
		return reportLocalPath
	}
	
	public boolean deleteReport(reportPath){
		return false
	}
	
	public boolean login(){
		
		def postData = "{\"KeyId\": \"<KEYID>\",\"KeySecret\": \"<SECRET>\"}"
		postData = postData.replaceAll("<KEYID>", apiKeyId)
		postData = postData.replaceAll("<SECRET>", apiKeySecret)
		def headers = [:]
		headers["Content-Type"] = "application/json"
		def response = request(baseUrl + "/api/V2/Account/ApiKeyLogin", "POST", headers, postData)
		
		if(response.code != 200){
			println("ASoC Login Failed")
			return false
		}
		def jsonSlurper = new JsonSlurper()
		def jsonObj = jsonSlurper.parseText(response.body)
		bearerToken = jsonObj.Token
		
		return bearerToken!=null
	}
	
	public void logout(){
		if(bearerToken == null) {
			//no need to logout if already logged out
			return
		}
		def headers = [:]
		headers["Accept"] = "application/json, text/plain, */*"
		headers["Authorization"] = "Bearer " + bearerToken
		def response = request(baseUrl+"/api/V2/Account/Logout", "GET", headers, null)
		if(response.code != 200){
			println("Failed to Logout - Response Code " + response.code)
		}
		else{
			//clear bearer token if logged out
			bearerToken = null
		}
	}
	
	private RestResponse request(url, method="GET",Map<String, String> headers, data=null){
		
		def response = new RestResponse()
		
		//Make Sure we have valid request method
		method = method.toUpperCase()
		if(!method.equals("POST") && !method.equals("PUT") && !method.equals("GET") && !method.equals("DELETE")){
			println("Invalid HTTP Method: " + method)
			return response
		}
		
		//Handle Get Parameters
		//Make sure data is of type Map<String, String>
		//build the query parameter string and append to URL
		if(method.equals("GET") && data != null)
		{
			try{
				assert data instanceof Map<String, String>: "data should be of type Map<String, String> for GET requests"
				def queryString = "?"
				def first = true
				for(param in data){
					if(!first){
						queryString += "&"
					} else{
						first = false
					}
					queryString += param.getKey() + "=" + param.getValue()
				}
				url += queryString
			} catch(AssertionError e){
				println("Error creating request:" + e.getMessage())
				return null
			}
		}
		
		HttpURLConnection connection = (HttpURLConnection) url.toURL().openConnection()

		if (headers != null) {
			for (String headerKey : headers.keySet()) {
				connection.addRequestProperty(headerKey, headers.get(headerKey))
			}
		}
		
		connection.setRequestMethod(method)
		if((method.equals("POST") || method.equals("PUT")) && data != null)
		{
			connection.doOutput = true
			connection.outputStream.withWriter{
				it.write(data)
				it.flush()
			}
		}
		
		
		connection.connect()
		
		//Determine if content is binary or text based
		def binaryContent = false
		def contentType = connection.getHeaderField("Content-Type")
		if(contentType != null && contentType.contains("application/octet-stream")){
			binaryContent = true
		}

		try {
			if(binaryContent){
				response.body = connection.getInputStream().getBytes()
			}else{
				response.body = connection.getInputStream().getText()
			}
			response.headers = connection.getHeaderFields()
			
		} catch (IOException e) {
			def errorMsg
			if(connection.errorStream!=null){
				errorMsg = connection.errorStream.text
			} else{
				errorMsg = e.getMessage()
			}
			
			println("Error making request to " + url + ": " + errorMsg)
			response.body = errorMsg

		} finally{
			response.code += connection.getResponseCode()
		}

		return response
	}
	
	private class RestResponse{

		int code = 0
		Map<String, List<String>> headers
		def body = null
		
		public void print(){
			println("Response Code: " + code)
			for(h in headers){
				def line = ""
				if(h.getKey()!=null){
					line += h.getKey() + ": "
				}else{
					line += code + " "
				}

				if(h.getValue() instanceof List<String>){
					for(val in h.getValue()){
						line += val + " "
					}
				}else{
					line += h.getValue()
				}
				println(line)
			}
			println()
			
			if(body instanceof byte[]){
				println("<Binary Content>")
			}else{
				println(body)
			}
		}
	}
	
	public String htmlEncode(String input){
		//only encodes html breaking chars - not a complete list
		
		if(input==null) return ""
		
		String output = ""
		
		def charArray = input.toCharArray()
		def encoded = ""
		for(c in charArray){
			switch(c){
				case '>':
					encoded = "&gt"
					break
				case '<':
					encoded = "&lt"
					break
				case '&':
					encoded = "&amp"
					break
				case '"':
					encoded = "&quot"
					break
				default: encoded = c
			}
			output += encoded
		}
		
		return output
	}
}