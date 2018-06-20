 var expiresIn = context.getVariable("private.authResponse.expiresIn");
 // secs to msec
 expiresIn = expiresIn * 1000;
 context.setVariable("expiresInMsec", expiresIn.toString());
