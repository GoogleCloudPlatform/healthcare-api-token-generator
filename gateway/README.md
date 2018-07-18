# Building and deploying the Apigee proxies

This directory contains the source code and build scripts for two Apigee API proxies.  They are:

* "oauth-b2b", which implements the OAuth 2 client credentials grant protocol.  This API returns a bearer token which
can be used to make requests for data.
* "fhir-endpoints", which validates a token and formats a data request to an HCLS FHIR API endpoint.


## Before you begin
Please review the documentation in the README.md file in the root directory of this repository.  This file contains
important information about the general build process, setting and using build profile parameters, and preparing for
the use of Google's container repository.

You must have the name of your Apigee organization and environment available before proceeding, and your Apigee login
must have sufficient privileges to import and deploy API proxies, create or modify encrypted Apigee key-value maps (KVMs),
and create or configure Apigee Developer and Developer Application entities.

Before deploying the Apigee proxies you should build and deploy the Kubernetes service on which these proxies depend.
Refer to the README.md file in this repository's root directory for more details.


## Configuring the proxies before deployment

Before you deploy the Apigee API proxies, you must configure the "oauth-b2b" proxy to know where your newly-deployed
Kubernetes service resides.  To do so:

* Navigate to the "apiproxy/targets" directory
* Open the "TargetEndpoint-1.xml" file and locate the line containing the placeholder "{k8s-ip-addr-or-dns-name}".
* Change the placeholder to the value of the DNS name or IP address of the service, as appropriate.
* Save and close the file.

The Apigee API policy language and product features make configuring API proxies very easy.  This sample has a basic
set of capabilities, but it's quite possible to extend these proxies to enforce your own traffic management, threat
detection, and mediation policies.


## Packaging, importing and deploying the proxies
To make these proxies available in your Apigee environment, execute the following command from the "gateway" directory:

``` mvn clean deploy -P <profile-name> -Dusername=<apigee-userid> -Dpassword=<apigee-password> -Dorgname=<apigee-orgname> ```

where "<profile-name>" is the Maven profile name, "<apigee-userid>" and "<apigee-password>" are the credentials for the
Apigee account to be used when importing and deploying proxies, and "<apigee-orgname>" is the Apigee organization name.
In this example the Apigee environment name to be used to deploy the proxies is specified in the Maven profile, but this
can be overridden on the command line as well.


## Generating IAM service account keys
Information on how to create IAM service account keys can be found in the [GCP documentation](https://cloud.google.com/iam/docs/creating-managing-service-account-keys).
Service keys you create for use with the Cloud Healthcare APIs should have the appropriate permissions for the type of
data to be accessed (DICOM or FHIR, for example) and the nature of the access to be granted (read only, read/write, etc.)

Note that you will not be able to assign Cloud Healthcare API permissions to a service account key unless the Healthcare
API has been enabled for your project.


## Creating an encrypted KVM and storing IAM service account keys
Encrypted key-value maps (KVMs) can be created using either the Apigee UI or its Management API.  Please see the
[Apigee documentation](https://docs.apigee.com/api-platform/cache/key-value-maps) for information on how to create KVMs.

Once the encrypted KVM is created, you can insert the IAM service account key with a name that matches the name of the
Apigee application definition.  This name is used to find the appropriate IAM key for a given application.


## Configuring Developer and Developer Application entities
Apigee developer and developer application entities provide metadata about applications that can access APIs hosted in
Apigee and the developers that created them.  You can use the Apigee UI or its management API to create these entities;
for more information, please see the [corresponding Apigee documentation](https://docs.apigee.com/api-platform/publish/publishing-overview).

Once developer and developer application entities have been created for an application that should access the Healthcare
API, you can use metadata on the application entity to indicate which project, location, dataset and store are to be made
available.  You add this information to the Developer Application entity using Apigee's "custom attributes" feature.

Four values need to be created in custom atributes for each application:

* "project" - the name of the GCP project in which the Healthcare API is hosted.
* "location" - the location in which the Healthcare API dataset was created
* "dataset" - the name of the dataset to which access is to be granted
* "fhirstore" - the name of the FHIR data store to which access is to be granted.

Once these values are set, the API proxies provided in this solution will automatically map them to the appropriate
Healthcare API calls.


## Next steps
If the Kubernetes service and Apigee API proxies are installed, you're ready to go!  Sample calls for using the APIs are
available in the "Testing the Implementation" section of the README.md doc in the root of this repository.



