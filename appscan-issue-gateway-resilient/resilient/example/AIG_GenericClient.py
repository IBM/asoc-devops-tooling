import argparse
import os
import sys
import json
import requests
import time

parser = argparse.ArgumentParser()
parser.add_argument("host", help="AppScan Issue Gateway Host Name")
parser.add_argument("port", type=int, help="AppScan Issue Gateway Port Number")
parser.add_argument("jsonfile", help="Path to JSON file to submit to AIG")
parser.add_argument("-r", "--repeat", type=int, default=0,
                    help="Minutes to wait between repeats")
args = parser.parse_args()

if(os.path.exists(args.jsonfile)==False):
	print("JSON file does not exist")
	sys.exit(1)

jsonStr = ""
with open(args.jsonfile) as f:
	jsonStr = f.read()

try:
	jsonObj = json.loads(jsonStr)
except ValueError:
	print("JSON file does not contain valid JSON")
	sys.exit(1)
	
url = "http://" + args.host + ":" + str(args.port)
headers = {"Content-Type": "application/json","Accept": "application/json"}
while True:
	#make the initial request to 
	r = requests.post(url + "/issues/pushjobs", headers=headers, data=jsonStr)
	print(r.text)
	jsonObj = r.json()
	id = jsonObj["id"]
	params = {"id":id}
	prevStatus = jsonObj["status"]
	
	#monitor the job until completion
	#only print response when it changes
	while prevStatus.find("Complete") == -1:
		time.sleep(10)
		r = requests.get(url + "/issues/pushjobs", headers=headers, params=params)
		jsonObj = r.json()
		newStatus = jsonObj["status"]
		if(newStatus != prevStatus):
			prevStatus = newStatus
			print(r.text)
		
	print("AppScan Issue Gateway Job Complete")
	if(args.repeat==0):
		sys.exit(0)
	else:
		print("Waiting " + str(args.repeat) + " minutes to repeat the job")
		time.sleep(args.repeat * 60)