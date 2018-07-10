# Example script to demonstrate static scan automation with
# IBM Application Security on Cloud and SAClient.py
# Prerequisites: 
#	1. Have an IBM ASoC account with API Key
#	2. Install the SAClientUtil and add /bin to the path (tested with SAClient Version 6.0.1222)
#	3. Install Python 3, (This was developed and tested on 3.6.5)
#	4. Have an application to scan and a well configured appscan-config.xml file
# Author: Cody Travis
# Email: cwtravis@us.ibm.com
# Date: 2018-05-07

import SAClient
import sys
import datetime
import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument("keyId", help="ASoC API KeyID to login to ASoC")
parser.add_argument("keySecret", help="ASoC API Key Secret to login to ASoC")
parser.add_argument("appId", help="ASoC AppId to associate scan results")
parser.add_argument("-c", "--config", help="AppScan Config XML File")
parser.add_argument("-s", "--scan", help="Scan Name for the ASoC Portal")

args = parser.parse_args()

if(args.config is not None and os.path.exists(args.config)==False):
	print("Config file does not exist")
	sys.exit(1)
else:
	configFile = args.config

if(args.scan is not None):
	scanName = args.scan
else:
	scanName = "Static_Scan"
	
apiKeyId = args.keyId
apiKeySecret = args.keySecret
appId = args.appId

# Step 1: Generate IRX File
print("=== Step 1: Generate the IRX File ===")
irxFile = SAClient.generateIRX(scanName, configFile)
if(irxFile is None):
	print("Something went wrong generating IRX File")
	sys.exit(1)
scanName = irxFile
irxFile = irxFile + ".irx"
print("IRX File Generated: " + irxFile)
print()

# Step 2: Login to the ASoC Service
print("=== Step 2: Login to the ASoC Service ===")
if(SAClient.loginASoC(apiKeyId, apiKeySecret)):
	print("Login Successful")
else:
	print("Login was unsuccessful")
	sys.exit(1)
print()

# Step 3: Verify Application Id Exists
print("=== Step 3: Verify Application Id Exists ===")
appList = SAClient.getAppList()
if(appId in appList):
	print("Application Id exists in ASoC")
	print("App Name: [" + appList[appId] + "] AppId: [" + appId + "]")
else:
	print("AppId does not exist or you don't have permission to view it")
	sys.exit(1)
print()

# Step 4: Submit IRX File for Analysis
print("=== Step 4: Submit IRX File to ASoC for Analysis ===")
#For demo purposes use demo.irx
irxFile = "example/demo.irx" #Comment this line to use the IRX File generated in Step 1
scanId = SAClient.queueAnalysis(irxFile, scanName, appId)
if(scanId is None):
	print("Something went wrong while submitting the IRX File")
	sys.exit(1)
else:
	print("Scan Created: " + scanId)
print()

# Step 5: Wait for the scan to complete
print("=== Step 5: Wait for the scan to complete ===")
result = "Failed"
try:
	result = SAClient.waitForScan(scanId, printStatusEveryMins=5)
except UnauthenticatedException:
	print("Login token has expired. Logging in again and continue waiting for scan to complete")
	if(SAClient.loginASoC(apiKeyId, apiKeySecret)):
		print("Login Successful")
	else:
		print("Login was unsuccessful")
		sys.exit(1)
	result = SAClient.waitForScan(scanId, printStatusEveryMins=5)
	
if(result != "Ready"):
	print("Problem waiting for scan to finish: Reason - " + result)
	sys.exit(1)
print("Scan is ready")
print()

# Step 6: Retrieve Scan Results
print("=== Step 6: Retrieve Scan Results and Save to File ===")
summary = SAClient.getScanSummary(scanId, scanName+"_result.json")
if(summary is None):
	print("Something went wrong getting the scan summary")
	sys.exit(1)
	
print("Scan Summary:")
print("  Status: " + summary["LatestExecution"]["Status"] + " (" + summary["LatestExecution"]["ExecutionProgress"] + ")")
startTime = datetime.datetime.strptime(summary["LatestExecution"]["CreatedAt"], '%Y-%m-%dT%H:%M:%S.%fZ')
endTime = datetime.datetime.strptime(summary["LatestExecution"]["ScanEndTime"], '%Y-%m-%dT%H:%M:%S.%fZ')
print("  Scan Duration: " + SAClient.strfdelta(endTime-startTime, "{days}d {hours}h {minutes}m {seconds}s"))
print("  High Issues: " + str(summary["LatestExecution"]["NHighIssues"]))
print("  Medium Issues: " + str(summary["LatestExecution"]["NMediumIssues"]))
print("  Low Issues: " + str(summary["LatestExecution"]["NLowIssues"]))
print("  Info Issues: " + str(summary["LatestExecution"]["NInfoIssues"]))
print()
print("  Total Issues: " + str(summary["LatestExecution"]["NIssuesFound"]))
print("Saved Scan Summary JSON File: " + scanName+"_result.json")
print("Use this file for further automation.")
print()

# Step 7: Download HTML Report
print("=== Step 7: Download HTML Report ===")
result = SAClient.getReport(scanId, scanName+"_report.html", "html")
if(result == False):
	print("Problem downloading report")
	sys.exit(1)

print("Report Downloaded and saved to " + scanName+"_report.html")
print()

print("=== Static Scan Automation Complete ===")

#Copyright 2018 Cody Travis

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#  http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.