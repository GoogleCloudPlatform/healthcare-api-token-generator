package netgloo.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.healthcare.apigee.GCPAuthJavaCallout;
import com.google.healthcare.apigee.OAuthTokenRequest;
import com.google.healthcare.apigee.OAuthTokenResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {
  
  // The Environment object will be used to read parameters from the 
  // application.properties configuration file
  @Autowired
  private Environment env;
  
  /**
   * POST /uploadFile -> receive and locally save a file.
   * 
   *
   * @return An http OK status in case of success, an http 4xx status in case 
   * of errors.
   */
  @RequestMapping(value = "/oauth2/token",
          method = RequestMethod.POST,
          consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE },
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  public ResponseEntity<?> getToken (@RequestBody OAuthTokenRequest tokenRequest) {

    try {
      final GCPAuthJavaCallout callout = new GCPAuthJavaCallout();
      final OAuthTokenResponse tokenResponse = callout.getToken(tokenRequest);
      final String jsonOutput = String.format("{ \"accessToken\" : \"%s\", \"expiresIn\" : %d }",
              tokenResponse.getAccessToken(),
              tokenResponse.getExpiresin()
      );

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_JSON);

      return new ResponseEntity<String>(jsonOutput, responseHeaders, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return exceptionResponse(e, HttpStatus.BAD_REQUEST);
    } catch (IOException e) {
      return exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Throwable e) {
      return exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
  } // method getToken


  /**
   * Generate an error response from a Java Throwable
   *
   * @return
   */
  private ResponseEntity<String> exceptionResponse(final Throwable e, final HttpStatus status) {
    final String response = String.format("{ \"error\" : \"%s\" }",
            e.getMessage()
    );

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.APPLICATION_JSON);

    return new ResponseEntity<>(response, responseHeaders, status);
  } // method exceptionResponse



} // class MainController

