include Configfile
.PHONY: config-arch asoc-tool clone-repo generate-irx api-login \
	upload-file get-app run-scan show-scan-id get-asset-group create-app

# This will configure a 32-bit architecture on top of a 64-bit linux machine
# Can potentially remove this in the future.
config-arch:
	sudo dpkg --add-architecture i386
	sudo apt-get update
	sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386

# Gets the ASoC Client Tool and configures it.
asoc-tool: config-arch
	$(eval DIR := $(shell pwd))
	curl -o client.zip $(APPSCAN_TOOL)
	mkdir client ; mkdir tool
	unzip -qq client.zip -d client
	cd client ; ls | xargs -I {} sh -c "cp -r {}/* $(DIR)/tool"
	rm -rf client

# Clones the target github repository into your travis environment.
clone-repo:
	git clone $(GIT_REPO)

# Generates the irx file for icp-cert-manager.
generate-irx: 
	$(eval DIR := $(shell pwd))

	cd $(PROJECT_NAME); $(DIR)/tool/bin/appscan.sh prepare $(flag)

# Login to the AppScan API.
api-login:
	curl -o token.json -X POST $(CONTENT_HEADER_JSON) $(ACCEPT_HEADER_JSON) -d '{"KeyId":$(API_KEY), "KeySecret":$(SECRET)}' $(LOGIN_URL)

# Uploads the irx file to the AppScan API.
upload-file: api-login
	$(eval TOKE := $(shell python getJson.py token.json "Token"))
	$(eval AUTH := --header 'Authorization: Bearer $(TOKE)')
	$(eval FILE := fileToUpload=@$(shell pwd)/$(PROJECT_NAME)/$(notdir $(shell find $(pwd) -maxdepth 2 -name '*.irx' -print)))

	curl -o file.json -X POST --header 'Content-Type: multipart/form-data' $(ACCEPT_HEADER_JSON) $(AUTH) -F $(FILE) $(UPLOAD_URL)

# Checks to see if Cert-Manager-Application already exists.
# TODO: Error with the url, will come back to this later.
get-app:
	$(eval TOKE := $(shell python getJson.py token.json "Token"))
	$(eval AUTH := --header 'Authorization: Bearer $(TOKE)')
	$(eval URL := $(GET_APP_URL)'$(APP_NAME)''')

	curl -X GET $(ACCEPT_HEADER_JSON) $(AUTH) $(URL)

# Assume we have an existing application, then we'll simply run the static scan.
run-scan:
	$(eval TOKE := $(shell python getJson.py token.json "Token"))
	$(eval AUTH := --header 'Authorization: Bearer $(TOKE)')
	$(eval FILE_ID := "$(shell python getJson.py file.json "FileId")")
	$(eval APP_ID := "$(shell python getJson.py app.json "Id")")

	curl -o scan.json -X POST $(CONTENT_HEADER_JSON) $(ACCEPT_HEADER_JSON) $(AUTH) -d '{"ARSAFileId": $(FILE_ID), "ApplicationFileId": $(FILE_ID), "ScanName": "StaticAnalyzer", "EnableMailNotification": false, "Locale": "en-US", "AppId": $(APP_ID), "Execute": true, "Personal": false}' $(STATIC_SCAN_URL)

# Outputs the id of the scan.
show-scan-id:
	@echo 'SCAN ID: ${shell python getJson.py scan.json "Id"}'

# Gets the asset group id that's needed to create an application.
get-asset-group:
	$(eval TOKE := $(shell python getJson.py token.json "Token"))
	$(eval AUTH := --header 'Authorization: Bearer $(TOKE)')

	curl -o asset.json -X GET $(ACCEPT_HEADER_JSON) $(AUTH) $(GET_ASSET_GROUP_URL)

# Create the application only if the application doesn't already exist.
create-app: get-asset-group
	$(eval ASSET_GROUP_ID := "$(shell python getJson.py asset.json "Id")")
	$(eval TOKE := $(shell python getJson.py token.json "Token"))
	$(eval AUTH := --header 'Authorization: Bearer $(TOKE)')

	curl -o app.json -X POST $(CONTENT_HEADER_JSON) $(ACCEPT_HEADER_JSON) $(AUTH) -d '{"Name": $(APP_NAME), "AssetGroupId": $(ASSET_GROUP_ID), "BusinessImpact": "Unspecified"}' $(CREATE_APP_URL)