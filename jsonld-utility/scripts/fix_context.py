import json

def transform(key, json_object, result):
	json = {}
	if '@context' in json_object:
		json['@id']='dpp:'+key
		json['@type'] = '@id'
		transform('@context', json_object['@context'], json)
	else:
		for majorkey, subdict in json_object.items():		
			if isinstance(subdict, dict):
				transform(majorkey, subdict, json)
			else:
				if majorkey == '@version':
					continue
				else:
					json[majorkey]={}
					json[majorkey]['@id']='dpp:'+majorkey
					json[majorkey]['@type']=subdict	

	if '@container' in json_object:
		json['@container'] = json_object['@container']
	result[key] = json
	return result

			
# Opening JSON file
with open('ddp-context.jsonld', 'r') as openfile:

	# Reading from json file
	json_object = json.load(openfile)

	result = {}
	context = {}
	context['dpp'] = 'https://test.uncefact.org/vocabulary/untp/dpp/'
	context['xsd'] = 'http://www.w3.org/2001/XMLSchema#'
	context['id'] = '@id'
	context['type'] = '@type'
	context['@version'] = 1.1
	productPassport = {}
	productPassport['@id'] = 'dpp:ProductPassport'
	productPassport['@type'] = '@id'
	productPassport.update(transform('@context', json_object['@context'], {}))
	context['ProductPassport'] = productPassport
	result['@context'] = context
	 
# Serializing json
json_object_s = json.dumps(result, indent=4)
 
# Writing to sample.json
with open("sample.json", "w") as outfile:
    outfile.write(json_object_s)

