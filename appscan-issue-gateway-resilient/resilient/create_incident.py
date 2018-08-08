#!/usr/bin/env python

from __future__ import print_function
import time
import logging
import resilient
import json
import sys

logging.basicConfig()

class ExampleArgumentParser(resilient.ArgumentParser):
	"""Arguments for this command-line application, extending the standard Resilient arguments"""

	def __init__(self, config_file=None):
		super(ExampleArgumentParser, self).__init__(config_file=config_file)
		
		self.add_argument('--config', '-c', required=False, help="Configuration File Path")
		
		self.add_argument('--name', '-n', required=True, help="The incident name.")

		self.add_argument('--description', '-d', required=True, help="The incident description.")

		self.add_argument('--itype', '-t', action='append', help="The incident type(s).  Multiple arguments may be supplied.")
		
		self.add_argument('--attachment', '-a', required=False, help="Path of file to attach to incident")

def main():
	"""
	program main
	"""

	parser = ExampleArgumentParser(config_file=resilient.get_config_file())
	opts = parser.parse_args()

	inc_name = opts["name"]
	inc_desc = opts["description"]
	inc_types = opts["itype"]
	attachment = opts["attachment"]
	
	# Create SimpleClient for a REST connection to the Resilient services
	client = resilient.get_client(opts)

	# Discovered Date will be set to the current time
	time_now = int(time.time() * 1000)

	#Construct SearchExDTO to see if incident already exists
	searchExDTO = {
			"query": inc_name,
			"filters": {
				"incident": [
					{
						"conditions": [
						{
							"field_name": "name",
							"method": "equals",
							"value": inc_name
						},
						{
							"field_name": "plan_status",
							"method": "in",
							"value": [
							  "A"
							]
						}
						]
					}
				]
			},
			"types": [
				"incident"
			]
		}
	
	# Construct the basic incident DTO that will be posted
	new_incident = {"name": inc_name,
					"description": {
							 "format": "html",
							 "content": inc_desc
						},
					"incident_type_ids": inc_types,
					"discovered_date": time_now}
					

	try:
		
		# See if any active incidents exist with the same name
		search = client.search(searchExDTO)
		if(len(search['results']) > 0):
			#print("Incident already exists in IBM Resilient, not creating it again")
			print(searchExDTO)
			sys.exit(1)
		
		# Create the incident
		uri = '/incidents'
		incident = client.post(uri, new_incident)
		inc_id = incident["id"]
		#print("Created incident {}".format(inc_id))
		
		upload = client.post_attachment('/incidents/{0}/attachments'.format(inc_id), attachment)
		#print('Created attachment:  ', file=sys.stderr)
		print(json.dumps(upload, indent=4))
		#Normal Exit
		sys.exit(0)

	except resilient.SimpleHTTPException as ecode:
		print("create failed : {}".format(ecode))

if __name__ == "__main__":
	main()
