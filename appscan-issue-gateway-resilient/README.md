# Resilient Provider for AppScan Issue Gateway
This projects provides the capability to create an IBM Resilient Incident via AppScan Issue Gateway automation

# Prerequisites: 
  1. Have an IBM Application Security on Cloud account with API Key
  2. Have API access to an IBM Resilient instance
  3. Install AppScan Issue Gateway - See links for download
 
# Getting Started:
  1. Setup the AppScan Issue Gateway by downloading the latest version from the HCL GitHub.
  2. Clone the "resilient" directory from this repository to the "providers" directory in the AppScan Issue Gateway Install.
  3. Python and the Resilient python library will need to be installed "pip install resilient".
  4. Edit the "app.config" file to reflect your IBM Resilient Info. 
    *Export your TLS Certificate from your Resilient server and save. Add the path to the app.config file
	*Add username and password to app.config. For added security, use a keystore instead (see "Helpful Links" for help) 
	*The hostname on the certificate must match EXACTLY the hostname for the Resilient server.
  5. Edit the example/resilient.json file to suit your needs. You will need to update it with your API Key/Secret and App ID.
  6. Use your favorite client to submit the resilient.json file to AppScan Issue Gateway. I have included a generic client in the example directory. For more info on that, see below.

#The Generic Client
AIG_GenericClient.py is included in the example directory. It uses "Requests" python library to submit your JSON file to AppScan Issue Gateway and monitor its progress.
For useage instructions issue `python AIG_GenericClient.py -h`
  python AIG_GenericClient.py -h
  usage: AIG_GenericClient.py [-h] [-r REPEAT] host port jsonfile

  positional arguments:
    host                  AppScan Issue Gateway Host Name
    port                  AppScan Issue Gateway Port Number
    jsonfile              Path to JSON file to submit to AIG
  
  optional arguments:
    -h, --help            show this help message and exit
    -r REPEAT, --repeat REPEAT
                          Minutes to wait between repeats


# Run the Example:  
Print the help and usage information  
`python AIG_GenericClient.py -r 60 localhost 8080 ./resilient.json`  
This example will submit the resilient.json file to AppScan Issue Gateway running on localhost port 8080.
It will follow the request status to completion, printing out the status to the console.
It will also repeat the request every 60 minutes.

# Helpful Links:  
[IBM Resilient Python SDK Guide](https://developer.ibm.com/security/resilient/sdk/)  
[AppScan Issue Gateway GitHub](https://github.com/hclproducts/appscan-issue-gateway)  
	
# License
All files found in this project are licensed under the [Apache License 2.0](LICENSE).