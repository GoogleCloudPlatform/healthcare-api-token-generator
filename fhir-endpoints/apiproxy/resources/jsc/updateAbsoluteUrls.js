// Following function from Philipp's answer in
// https://stackoverflow.com/questions/10687099/how-to-test-if-a-url-string-is-absolute-or-relative
function isUrlAbsolute(url) {
  if (url.indexOf('//') === 0) {return true;} // URL is protocol-relative (= absolute)
  if (url.indexOf('://') === -1) {return false;} // URL has no protocol (= relative)
  if (url.indexOf('.') === -1) {return false;} // URL does not contain a dot, i.e. no TLD (= relative, possibly REST)
  if (url.indexOf('/') === -1) {return false;} // URL does not contain a single slash (= relative)
  if (url.indexOf(':') > url.indexOf('/')) {return false;} // The first colon comes after the first slash (= relative)
  if (url.indexOf('://') < url.indexOf('.')) {return true;} // Protocol is defined before first dot (= absolute)
  return false; // Anything else must be relative
}

// test if Cloud Healthcare API url
function isHealthcareApiUrl(url) {
  return (isUrlAbsolute(url) && url.indexOf('healthcare.googleapis.com') != -1);
}

var pathRe = new RegExp('/projects/[a-zA-Z0-9\-]+/locations/[a-zA-Z0-9-]+/datasets/[a-zA-Z0-9-]+/fhirStores/[a-zA-Z0-9-]+/fhir/');
// var pathRe = '/projects/[a-zA-Z0-9\-]+/locations/[a-zA-Z0-9-]+/datasets/[a-zA-Z0-9-]+/fhirStores/[a-zA-Z0-9-]+/fhir/';

// Replace Cloud Healthcare API URL with one for this proxy's DNS and base path
function relocateHealthcareApiUrl(newProtocol, newDomain, newPath, oldUrl) {
  var re = pathRe.exec(oldUrl);
  if (re) {
    var pathSuffix = oldUrl.substring(re.index + re[0].length);
    var newUrl = newProtocol + "://" + newDomain + newPath + "/" + pathSuffix;
    return newUrl;
  } else {
    return oldUrl;
  }
}


function recursiveUrlFix(newProtocol, newDomain, newPath, entity) {
  if (Array.isArray(entity)) {
    for (var i = 0; i < entity.length; i++) {
      if (typeof entity[i] == 'string') {
        if (isHealthcareApiUrl(entity[i])) {
          entity[i] = relocateHealthcareApiUrl(newProtocol, newDomain, newPath, entity[i]);
        }
      } else if (typeof entity[i] == 'object') {
        entity[i] = recursiveUrlFix(newProtocol, newDomain, newPath, entity[i]);
      }
    }
  } else {
    var keyIndexes = Object.keys(entity);
    for (var keyIndex in keyIndexes) {
      var key = keyIndexes[keyIndex];
      if (entity.hasOwnProperty(key) && (entity[key] !== null)) {
        if (typeof entity[key] == 'string') {
          if (isHealthcareApiUrl(entity[key])) {
            var newUrl = relocateHealthcareApiUrl(newProtocol, newDomain, newPath, entity[key]);
            entity[key] = newUrl;
          }
        } else if (typeof entity[key] == 'object') {
          entity[key] = recursiveUrlFix(newProtocol, newDomain, newPath, entity[key]);
        }
      }
    }
  }

  return entity;
}

 var scheme = context.getVariable("client.scheme");
 var host = context.getVariable("savedHostName");
 var basePath = context.getVariable("proxy.basepath")
 var contentType = context.getVariable("response.header.Content-Type");
 if (contentType.indexOf("json") != -1) {
     context.setVariable("entered", true);
     try {
        var responseContent = context.getVariable("response.content");
        var parsedResponseContent = JSON.parse(responseContent);
        var newResponseContent = recursiveUrlFix(scheme, host, basePath, parsedResponseContent);
        context.setVariable("response.content", JSON.stringify(newResponseContent));
     } catch (err) {
         // let malformed JSON, etc. flow back to the client as-is
         context.setVariable("script-error", ('Unknown error: ' + err.messsage + " at line " + err.lineNumber));
     }
 }
