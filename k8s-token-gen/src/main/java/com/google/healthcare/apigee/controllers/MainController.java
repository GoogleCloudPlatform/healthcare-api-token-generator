/*
 * Copyright (c) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.healthcare.apigee.controllers;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.healthcare.apigee.GCPAuthJavaCallout;
import com.google.healthcare.apigee.OAuthTokenRequest;
import com.google.healthcare.apigee.OAuthTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  
  /**
   * POST /oauth2/token -> Generate an OAuth token using the specified request parameters.
   * 
   * @param tokenRequest An object containing OAuth2 token request parameters from Apigee.
   * @return An HTTP 200 OK status in case of success, an HTTP 400 BAD REQUEST status in case
   * of an invalid request, and an HTTP 500 INTERNAL SERVER ERROR status in case of any other error.
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

      final String jsonOutput = OBJECT_MAPPER.writeValueAsString(tokenResponse);

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_JSON);

      return new ResponseEntity<>(jsonOutput, responseHeaders, HttpStatus.OK);
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

