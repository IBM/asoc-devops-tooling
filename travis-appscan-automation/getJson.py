import json
import sys

# Loads a json file in and gets the field specified.
# Usage: python getJson.py fileName "FieldName"
# Limitations: Only works for JSON objects and the field name
# is case-sensitive.

def main():
    data = load_data()
    printFields(data)

# Gets the contents of the JSON file specified.
def load_data():
    data = ""
    filename = sys.argv[1]
    with open(filename, "r") as read:
        data = json.load(read)
    if isinstance(data, list):
        data = data[0]
    return data

# Prints the specified field.
def printFields(data):
    fields = sys.argv[2:]
    for i in fields:
        print(data[i])

main()