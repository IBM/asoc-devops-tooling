/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package resilient
import common.IAppScanIssue

public class RESILIENTConstants {
	
	//Provider name
	static def PROVIDER_NAME = "resilient"

	//Required fields
	static def SERVER_URL  = "url"
	static def USERNAME    = "username"
	static def PASSWORD    = "password"
	static def PROJECTKEY  = "projectkey"
	static def CONFIG_FILE_PATH = "";
	
	//Optional fields
	static def SEVERITYMAP = "severitymap"
	static def SUMMARY     = "summary"
	static def OTHERFIELDS = "otherfields"
	
	//Description
	static def PROVIDER_DESCRIPTION = ["Resilient Incident Response provider. Configuration fields are below"]
	}