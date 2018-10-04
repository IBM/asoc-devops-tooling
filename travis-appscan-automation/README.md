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
1. You have an account on [AppScan](https://appscan.ibmcloud.com/AsoCUI/serviceui/home) already. [If you need an account, see instructions here.](https://w3-connections.ibm.com/wikis/home?lang=en-us#!/wiki/W2d45edf688c2_4f09_9d5d_bbddab46412e/page/ASoC%20Onboarding)
2. You have obtained an API Key and a Secret from the AppScan website.
3. The file types in your project are supported by the ASoC tool. [Static analyzer supported types](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/src_sys_req.html#src_sys_req__scan) and [open source supported types](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_scanning_opensource.html) 
4. Your repository is connected to [Travis](https://travis-ci.org).
## Adoption
#### In the Configfile, change
1. APP_NAME - The name of your application on the AppScan site.
2. PROJECT_NAME - The name of your github repository. 
#### In the Travis Settings, add
1. API_KEY - your API Key used to access the AppScan [click here to get it](https://appscan.ibmcloud.com/AsoCUI/serviceui/main/admin/apiKey)
2. SECRET - the secret that was generated when you generate the API Key above
3. Add your github SSH key to the SSH Key section if your repository requires an ssh key to clone.
#### If you have an application on the AppScan site, get your Application ID from the UI for the API
1. Go to https://appscan.ibmcloud.com/swagger/ui/index#!/Apps/Apps_GetApps and filter for your application.
2. Copy the JSON response from the UI into the app.json file.
#### If you don't already have an application on the AppScan site
1. In the .travis.yml file, BEFORE "make run-scan" in the script section, add: - make create-app 
2. Make sure to change the app name to what you want to name it in the Configfile (see above).
#### If you also want to do static analysis scanning rather than just open-source scanning
1. Remove `flag="-oso"` where it says `make generate-irx` from the .travis.yml file.
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
1. Kicking off the build will only start the static source code scan on AppScan, but will not update you of the results automatically. You must go to the AppScan website to view your scan.
2. The automation will not be able to query for an existing application on the AppScan site. Due to this constraint, if you already have an application on the AppScan site, make sure you update the app.json file as specified above in the Adoption section.
