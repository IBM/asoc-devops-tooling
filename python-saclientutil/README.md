# python-saclientutil
This project is a Python wrapper around the IBM Application Security on Cloud SAClient utility.
The SAClient.py script will aid in automation workflows where IBM/HCL do not have out-of-the box plug-ins or integrations.

# Prerequisites: 
  1. Have an IBM Application Security on Cloud account with API Key
  2. Install the SAClientUtil and add /bin to the path (tested with SAClient Version 6.0.1222)
  3. Install Python 3 (tested on 3.6.5)
  4. Have an application to scan and a well configured appscan-config.xml file.
 
# Getting Started:

Start with the example script SAST_Automation_Example.py. This script will step through a simple static analysis automation workflow.
  1. Generate an IRX file using the provided appscan-config.xml file.
  2. Login to the ASoC service.
  3. Verify that the provided AppId exists in your ASoC subscription.
  4. Submit the IRX file for analysis, using the provided scan name.
  5. Monitor the scan progress and wait for it to complete.
  6. Download the scan summary as a json file and print some info about the scan.
  7. Download the scan report to a desired location.

By default, the example will not upload the irx file generated in step 1. It will upload the sample IRX file included in the example directory (demo.irx)
This file will scan very quickly for demo purposes. In a real use case, delete or comment out the line that uses demo.irx instead of the real IRX file.

The war file, included in the example directory is the Altoro Mutual (altoromutual.war). This is an application specifically designed to be scanned using IBM Security tools. It is publicly available at:  
<https://github.com/AppSecDev/AltoroJ>

# Run the Example:  
Print the help and usage information  
`python SAST_Automation_Example.py -h`  

  
Run the example workflow  
`python SAST_Automation_Example.py <apiKeyId> <apiKeySecret> <ASoC AppId> -c example/appscan-config.xml -s SAST_Scan`  


# Helpful Links:  
[IBM Application Security on Cloud Knowledge Center](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/helpindex.html)  
	
# License

All files found in this project are licensed under the [Apache License 2.0](LICENSE).