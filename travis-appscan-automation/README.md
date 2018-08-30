# IBM Static Scan on Travis Integration
## Scope
The Static Scan is performed on [ICP-Cert-Manager](https://github.ibm.com/IBMPrivateCloud/icp-cert-manager), and it is automated using Travis. ICP-Cert-Manager scans are performed using the -oso flag since the repository is written in GoLang. The automation makes use of both the SAClientUtil (ASoC's Static Analyzer Client Utility) and the [AppScan API](https://appscan.ibmcloud.com/swagger/ui/index). The build is built on top of an Ubuntu 14.04 machine.

## Automation Overview
When a Travis build is kicked off, the build will:
1. Download the SAClientUtil tool
2. Download the project repository to be scanned
3. Run the SAClientUtil tool on the project repository (generating an .irx file)
4. Use the ASoC API to push the .irx file to the AppScan website (link)
5. Start the static analysis scan on the file

## Prerequisites
1. You have an account on [AppScan](https://appscan.ibmcloud.com/AsoCUI/serviceui/home) already. [If you need an account, register here]()
2. You have obtained an API Key and a Secret from the AppScan website.
## Adoption
#### In the Configfile, change
1. APP_NAME - The name of your application on the AppScan site.
2. PROJECT_NAME - The name of your github repository. 
#### In the Travis Settings, add
1. API_KEY - your API Key used to access the AppScan [click here to get it](https://appscan.ibmcloud.com/AsoCUI/serviceui/main/admin/apiKey)
2. SECRET - the secret that was generated when you generate the API Key above
## Files Overview
#### .travis.yml
The file used to conduct a Travis build. It calls make scripts from the Makefile in this folder to run the scripts needed to create the scan.
#### Makefile
These are all the scripts necessary to perform the static source code scanning. Everything from the prerequisites to the final push of the .irx file can be found here.
#### Configfile
The variables/constants used within the Makefile.
#### getJson.py
A simple python file for obtaining one JSON field from a JSON file. It's limited to JSON objects and will only extract the first object in a JSON array.

## Known Limitations
1. Kicking off the build will only start the static source code scan on AppScan, but will not update you of the results. You must go to the AppScan website to view your scan.
2. The automation will not be able to query for an existing application on the AppScan site. 
