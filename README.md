# Google Healthcare and Life Sciences API - OAuth 2 for Apigee

As part of its Healthcare and Life Sciences (HCLS) vertical the Google Cloud Platform (GCP) provides a set of APIs
and data stores that can be used to enable secure storage and retrieval of healthcare data, and also to initiate
processing of that data such as de-identification, validation and normalization, data analysis, and so on.
APIs and data stores are available for DICOM (imaging), HL7 v2 and FHIR modalities.

In order to gain access to and use these APIs, an OAuth2 access token is needed. For some applications tokens can be
generated using the "oauth2l" command line tool (https://github.com/google/oauth2l), but in many cases applications need
the ability to generate these tokens programmatically, particularly in long-running services or for situations where
data is being accessed by externally-facing applications where exposing sensitive credentials in not appropriate.  This
project contains an Apigee-based solution for enabling creation of these access tokens using the OAuth 2.0 client
credentials grant.  The solution is comprised of two pieces: a set of Apigee API proxies that enable token generation
and support access to the HCLS APIs, and a small Kubernetes-based service that uses GCP Identify and Access Management
(IAM) service account keys to generate access tokens.

Combined with application-specific configuration information governing which APIs and data stores can be accessed, and
Apigee's own extensive functionality for developer management, traffic mediation and threat detection,  a highly secure
and fully managed solution for controlling access to healthcare data can be created.


## Prerequisites
The following prerequisites are needed:

* An Apigee SaaS or on-premises installation.  Any Apigee release that supports encrypted KVMs can be used.  While you
may run the Apigee system on-premises in your data center or VPC, please keep in mind that the HCLS APIs are available
in Google Cloud Platform only.  Apigee has a free usage tier and other reasonably-priced pricing tiers; for more information
and links to pricing info, see [https://cloud.google.com/apigee-api-management/](https://cloud.google.com/apigee-api-management/).
* An available Apigee organization and environment.  See your Apigee administrator or API program manager to obtain this
information.
* A Kubernetes cluster of sufficient size to handle your expected token generation traffic.  This solution has currently
only been tested using GCP's Kubernetes Engine (GKE) offering; to get more info regarding GCP's Kubernetes environment,
see [https://cloud.google.com/kubernetes-engine/](https://cloud.google.com/kubernetes-engine/).
* The GCP SDK or an installation of the "gcloud" command line tool.
* Apache Maven, version 3 or later.
* A Java 8 Software Development Kit.
* A local running Docker environment


## Deploying the Solution

### Before you start
Before you begin to deploy this solution, you should be comfortable with the general concepts behind Docker and
Kubernetes.  Most of the "heavy lifting" is performed by the build and deployment process itself, but it's helpful to
know something about Docker in case something goes wrong.

We recommend that you have the following information available:

* The name of your Docker repository, and the credentials required to access it.  By default, the Maven-based build process
points to [gcr.io](gcr.io) and uses credentials from a current "gcloud" login, but you can change this if needed.
* The name of your Kubernetes cluster, and the permissions required to work with clusters, services and pods.  If you're
using GKE to host your Kubernetes service the GCP Console can also be used.
* Credentials for your Apigee instance, with the appropriate roles and permissions to create, manage and deploy new API
proxies, create and/or configure encrypted KVMs, and create and/or configure developers and developer applications.

It's also important to note that the build process uses Maven's "profile" functionality to enable environment-specific
deployments to be configured.  The master POM in this directory contains the profiles for "dev" and "prod"; these
profiles specify (for example) the Apigee management API endpoint.  You should review this before starting the deployment
process.

If you are deploying this solution to Google's Docker container registry, do the following to set "gcloud" as a Docker
credentials helper.  This enables your Google Cloud credentials to be used to deploy Docker images.

```
gcloud auth configure-docker
```

This step needs to be performed only once.  For more information on configuring Docker credentials for GCP, see
[https://cloud.google.com/container-registry/docs/advanced-authentication](https://cloud.google.com/container-registry/docs/advanced-authentication)


### The deployment process

To deploy this OAuth2 functionality to the Apigee system, do the following:

1. Clone this repository to your system.
2. Build and deploy the Kubernetes service contained in the "k8s-token-gen" subfolder to a Kubernetes cluster.  See the
README.md file in that folder for more details.
3. Once you have an IP address or DNS name for the newly-deployed Kubernetes code, edit the Apigee OAuth proxy to include
that information in the target endpoint, then import and deploy the Apigee proxies in your Apigee system.  See the
README.md file in the "gateway" subfolder for details on how to do this.
4. Configure Apigee to allow IAM service account keys to be stored in an encrypted key-value map (KVM) inside the
Apigee system, then create and configure developer and developer application definitions.  The README.md file in the
"gateway" subfolder has more information.
5. Use "curl" or your favorite API tool, plus the API key and secret from your Apigee developer application definition,
to issue a token request to Apigee.  More information on how to do this is found below.


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
When an application requests an Oauth access token to access the HCLS FHIR API, the steps shown in the diagram below
occur:

```
API
Consumer                          Apigee                      Kubernetes                 HCLS FHIR API
---------                       ----------                    ----------                 --------------
request token ----------------> validate access
using API key                   to HCLS FHIR API
and shared secret
                                Retrieve application
                                HCLS API config

                                Retrieve appropriate
                                IAM service acct key

                                Get IAM access token -------> Generate IAM
                                                              token

                                Receive IAM token    <------- Return token

                                Generate Apigee access
                                token and store IAM
                                token as related
                                metadata

token response  <-------------- Return Apigee token
```


Apigee uses its built-in capabilities to identify the requesting application based on the API key and secret presented
in the token request.  Once the application is identified, the information about the HCLS API stores the application
is eligible to access are retrieved, the appropriate IAM service key is retrieved from encrypted storage, and a request
is made to the Kubernetes service to generate an IAM access token. Once the IAM token is generated, Apigee generates an
access token of its own, associates the HCLS API info and IAM token with that self-generated token, and returns the
self-generated token to the requesting application.

When that application makes a request for data, the token is provided in the HTTP "Authorization" header as a "Bearer"
token:

```
API
Consumer                          Apigee                      Kubernetes                 HCLS FHIR API
---------                       ----------                    ----------                 --------------
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
                                HCLS FHIR API with
                                IAM token

                                Issue HCLS FHIR API
                                request              ------------------------------------> Receive FHIR
                                                                                           request

                                                                                           Validate IAM token

                                                                                           Retrieve data

                                Receive response     <------------------------------------ Return data
                                from HCLS FHIR API

Receive response <------------- Send response to client
```


The Kubernetes code is not involved in data request processing.


## Special considerations
* The Apigee API proxies in this solution do not currently contain any traffic management policies.  You can easily add
these and configure them to your specific requirements using the Apigee UI or your desktop text editor.


## Having problems?
If you run into any problems or issues with this solution, please file a ticket in the Github repository.

In addition, you can find help with general Apigee-related questions in the [Apigee documentation](https://docs.apigee.com)
or in the [Apigee Community](https://community.apigee.com)


