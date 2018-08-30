# IBM Static Scan on Travis Integration
-- 
## Scope
The Static Scan is performed on ICP-Cert-Manager (repo link), and it is automated using Travis. ICP-Cert-Manager scans are performed using the -oso flag since the repository is written in GoLang. The automation makes use of both the SAClientUtil (ASoC's Static Analyzer Client Utility)

## Automation Overview
When a Travis build is kicked off, the build will:
1. Download the SAClientUtil tool
2. Download the project repository to be scanned
3. Run the SAClientUtil tool on the project repository (generating an .irx file)
4. Use the ASoC API to push the .irx file to the AppScan website (link)
5. Start the static analysis scan on the file

## Prerequisites
1. You have an account on AppScan already

## Files Overview
.travis.yml
The file used to conduct a Travis build. It calls make scripts from the Makefile in this folder to run the scripts needed to create the scan.
Makefile
These are all the scripts necessary to perform the static source code scanning. Everything from the prerequisites to the final push of the .irx file can be found here.
getJson.py
A simple python file for obtaining one JSON field from a JSON file. It's limited to JSON objects and will only extract the first object in a JSON array.


