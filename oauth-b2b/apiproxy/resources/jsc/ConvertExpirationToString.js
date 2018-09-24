 var expiresIn = context.getVariable("expiresInSec");
 // secs to msec
 context.setVariable("expiresInMsec", (expiresIn * 1000).toString());
 // add 's' for JWT polic
 context.setVariable("expiresInSecJWT", expiresIn.toString() + 's');
