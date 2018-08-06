# Google Healthcare and Life Sciences API - OAuth 2 for Apigee

As part of its Healthcare and Life Sciences vertical the Google Cloud Platform (GCP) provides a set of APIs
and data stores that can be used to enable secure storage and retrieval of healthcare data, and also to initiate
data processing, data analysis, and so on. APIs and data stores are available for DICOM (imaging), HL7 v2 and FHIR
modalities.

In order to gain access to and use these APIs, an OAuth2 access token is needed. For some applications tokens can be
generated using the "oauth2l" command line tool (https://github.com/google/oauth2l), but in many cases applications need
the ability to generate these tokens programmatically, particularly in long-running services or for situations where
data is being accessed by externally-facing applications where exposing sensitive credentials in not appropriate.  This
project contains an Apigee-based solution for enabling creation of these access tokens using the OAuth 2.0 client
credentials grant.

This solution is comprised of two pieces:

* An Apigee API proxy called "oauth-b2b", which implements the OAuth 2 client credentials grant protocol.  This API proxy
returns a bearer token which can be used to make requests for data.
* An Apigee proxy called "fhir-endpoints", which validates a token generated using "oauth-b2b" and formats a data request
to a Cloud Healthcare FHIR API endpoint.

Combined with application-specific configuration information governing which APIs and data stores can be accessed, and
Apigee's own extensive functionality for developer management, traffic mediation and threat detection,  a highly secure
and fully managed solution for controlling access to healthcare data can be created.


## License, usage and maintenance
This project is being released under the Apache 2 license.  Please see the LICENSE file in this repository for details.

The code in this repository is intended primarily as a template for your own implementation. We strongly suggest that
you review the code before deployment to ensure that it is suitable for your specific requirements.  In particular:

* Because traffic management requirements are very specific to the customer and use case, the Apigee proxies in this
repository do not implement traffic management or threat detection policies.
* Generally speaking, the code in this repository is oriented more toward server-side applications within a healthcare
organization (although there's no reason that this code can't be used as a foundation for other use cases). For example,
this code uses the OAuth 2 client credentials grant, which does not accept user identify parameters (userid and password)
and is not appropriate for places where access is to be scoped to particular individuals (patients, providers, etc.).
This code also does not provide for any sort of end user consent management; if such features are needed, you may want
review Apigee's open-source Health APIx solution (https://github.com/apigee/flame).

PLEASE NOTE: THIS IS NOT AN OFFICIALLY SUPPORTED GOOGLE PRODUCT.


## Prerequisites
The following prerequisites are needed:

* An Apigee SaaS or on-premises installation.  Any Apigee release that supports encrypted KVMs and JWT generation can be
used.  While you may run the Apigee system on-premises in your data center or VPC, please keep in mind that the Cloud
Healthcare APIs are available in Google Cloud Platform only.  Apigee has a free usage tier and other reasonably-priced
pricing tiers; for more information and links to pricing info, see
[https://cloud.google.com/apigee-api-management/](https://cloud.google.com/apigee-api-management/).
* An available Apigee organization and environment.  See your Apigee administrator or API program manager to obtain this
information.
* An Apigee login with sufficient privileges to import and deploy API proxies, create or modify encrypted Apigee key-value
maps (KVMs), and create or configure Apigee Developer and Developer Application entities.
* The GCP SDK or an installation of the "gcloud" command line tool.
* Apache Maven, version 3 or later.
* A GCP IAM service account key for each application which will access the Cloud Healthcare APIs.  More information on
this is provided later in this document.

In addition, depending on your installation you may need to generate keys and certificates for use with TLS.  You should
have these available before you begin installation.


## Deploying the Solution

### Before you start
Most of the "heavy lifting" for the installation process is performed by Maven and the Apigee Maven Deploy Plugin.  You
may wish to become familiar with this plugin, and some of Apigee's other related Maven plugins.

It's important to note that the build process uses Maven's "profile" functionality to enable environment-specific
deployments to be configured.  The POM in this directory contains the profiles for "dev" and "prod"; these profiles
specify (for example) the Apigee management API endpoint.  You should review this before starting the deployment
process.



### Packaging, importing and deploying the proxies
To make these proxies available in your Apigee environment, execute the following command from the repository's root
directory:

``` mvn clean deploy -P <profile-name> -Dusername=<apigee-userid> -Dpassword=<apigee-password> -Dorgname=<apigee-orgname> ```

where "<profile-name>" is the Maven profile name, "<apigee-userid>" and "<apigee-password>" are the credentials for the
Apigee account to be used when importing and deploying proxies, and "<apigee-orgname>" is the Apigee organization name.
In this example the Apigee environment name to be used to deploy the proxies is specified in the Maven profile, but this
can be overridden on the command line as well.


### Generating IAM service account keys
Information on how to create IAM service account keys can be found in the [GCP documentation](https://cloud.google.com/iam/docs/creating-managing-service-account-keys).
Service keys you create for use with the Cloud Healthcare APIs should have the appropriate permissions for the type of
data to be accessed (DICOM or FHIR, for example) and the nature of the access to be granted (read only, read/write, etc.)

Note that you will not be able to assign Cloud Healthcare API permissions to a service account key unless the Healthcare
API has been enabled for your project.


### Creating an encrypted KVM and storing IAM service account keys
Encrypted key-value maps (KVMs) can be created using either the Apigee UI or its Management API.  Please see the
[Apigee documentation](https://docs.apigee.com/api-platform/cache/key-value-maps) for information on how to create KVMs.

Once the encrypted KVM is created, you can insert the IAM service account key with a name that matches the name of the
Apigee application definition for the application that will use the key.  This name is used to find the appropriate IAM
key for a given application.  While multiple applications can use the same key, each application must have a copy of the
key stored in the KVM.


### Configuring Developer and Developer Application entities
Apigee developer and developer application entities provide metadata about applications that can access APIs hosted in
Apigee and the developers that created them.  You can use the Apigee UI or its management API to create these entities;
for more information, please see the [corresponding Apigee documentation](https://docs.apigee.com/api-platform/publish/publishing-overview).

Once developer and developer application entities have been created for an application that should access the Healthcare
API, you can create metadata on the application entity to indicate which project, location, dataset and store are to be made
available.  You add this information to the Developer Application entity using Apigee's "custom attributes" feature.

Four values need to be created in custom atributes for each application:

* "project" - the name of the GCP project in which the Healthcare API is hosted.
* "location" - the location in which the Healthcare API dataset was created
* "dataset" - the name of the dataset to which access is to be granted
* "fhirstore" - the name of the FHIR data store to which access is to be granted.

Once these values are set, the API proxies provided in this solution will automatically map them to the appropriate
Healthcare API calls.


## Testing the implementation
Once the complete solution is deployed, you can generate an OAuth 2 token using a "curl" command similar to this one:

```
curl -X POST \
  https://<org>-<env>.apigee.net/oauth2/accesstoken \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic <base64-encoded-api-key-and-secret>' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -d '{ "grantType" : "client_credentials", "scopes" : "user/*.read" }'
```

With the token you can retrieve FHIR entities from the FHIR store using a command similar to this one:

```
curl -X GET \
  https://<org>-<env>.apigee.net/v1/hcapi/<entity-name>/<entity-id> \
  -H 'Accept: application/fhir+json;charset=utf-8' \
  -H 'Authorization: Bearer <access-token>' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/fhir+json;charset=utf-8'

```


## How it works
When an application requests an Oauth access token to access the Cloud Healthcare FHIR API, the steps shown in the diagram below
occur:

```
API                                                           Cloud Healthcare
Consumer                          Apigee                      FHIR API
---------                       ----------                    --------------
request token ----------------> validate access
using API key                   to Cloud Healthcare
and shared secret               FHIR API

                                Retrieve application
                                Cloud Healthcare API
                                config

                                Retrieve appropriate
                                IAM service account key

                                Get IAM access token
                                using information from
                                the GCP service account
                                key

                                Generate Apigee access
                                token and store IAM
                                token as related
                                metadata

token response  <-------------- Return Apigee token
```


Apigee uses its built-in capabilities to identify the requesting application based on the API key and secret presented
in the token request.  Once the application is identified, the information about the Cloud Healthcare API stores the
application is eligible to access are retrieved, the appropriate IAM service key is retrieved from encrypted storage, and
a JWT token is generated. Apigee then generates an access token of its own, associates the Cloud Healthcare API info and
JWT token with that self-generated token, and returns the self-generated token to the requesting application.  At no point
is information about GCP, the service account used, or the configured FHIR store provided to the requesting application.

When that application makes a request for data, the token is provided in the HTTP "Authorization" header as a "Bearer"
token:

```
API                                                           Cloud Healthcare
Consumer                          Apigee                      FHIR API
---------                       ----------                    --------------
Use bearer token
to
GET /Patient/1  --------------> Validate token

                                Retrieve associated
                                token metadata
                                (project, location,
                                dataset, FHIR store)
                                and IAM access token

                                Format request to
                                configured
                                Cloud Healthcare FHIR
                                API with JWT token

                                Issue Cloud Healthcare
                                FHIR API request     -------> Receive FHIR request

                                                              Validate IAM token

                                                              Retrieve data

                                Receive response     <------- Return data
                                from Cloud Healthcare
                                FHIR API

Receive response <------------- Send response to client
```


## Special considerations
* The Apigee API proxies in this solution do not currently contain any traffic management policies.  You can easily add
these and configure them to your specific requirements using the Apigee UI or your desktop text editor.


## Having problems?
If you run into any problems or issues with this solution, please file a ticket in the Github repository.

In addition, you can find help with general Apigee-related questions in the [Apigee documentation](https://docs.apigee.com)
or in the [Apigee Community](https://community.apigee.com)


