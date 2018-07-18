# Building the Kubernetes OAuth token generation service

This directory contains the source code and build scripts for a Kubernetes-hosted service that accepts information from
a specially-configured Apigee proxy and returns an IAM token generated using the Google Cloud Platform (GCP) Auth SDK.

## Before you begin
Please review the documentation in the README.md file in the root directory of this repository.  This file contains
important information about the general build process, setting and using build profile parameters, and preparing for
the use of Google's container repository.

Be sure to have Docker started on your build machine.  Docker is required to build the container that will run your
service.


## Building the service and pushing the container to your repository
To build the service, open a command-line prompt and navigate to the "k8s-token-gen" directory.  At the prompt, issue
the following command:

``` mvn clean deploy -P <profile-name> -Dgcp.project=<gcp-project-name> ```

where "<profile-name>" is the name of the Maven build profile you wish to use, and "<gcp-project-name>" is the name of
the GCP project to which you want to deploy this solution.

The build process will perform the following steps:

* Compile the service code and create a JAR file containing the service.
* Package the JAR into a Docker container, using the provided Dockerfile.
* Deploy the container to your container registry.  In the case of Google's registry, your GCP credentials will be used
to push the new container to "gcr.io".

When the build process completes a new container called "k8s-token-gen" will be stored in your image repository.  This new
container will have a tag that corresponds to the artifact version set in the Maven POM (by default, "1.0-SNAPSHOT").


## Deploying the service to an image repository
Once the "k8s-token-gen" container is built, you can create a new cluster and deploy the container.  The following
commands are appropriate for building a basic cluster and service on Google's Kubernetes Engine (GKE); you may change
these commands to create a service deployment that is more appropriate for your particular needs.

First, create a new cluster to hold your service.

``` gcloud container clusters create <cluster-name> --region <region> --zone <zone> --project <gcp-project-name> ```

where "<cluster-name>" is the name of your cluster, "<region>" is the GCP region in which you want to create your cluster,
"<zone>" is the zone in which your cluster is to be created, and "<gcp-project-name>" is the name of your GCP project.

When cluster creation is complete, obtain credentials for your cluster using the command below:

``` gcloud container clusters get-credentials <cluster-name> --region <region> --zone <zone> --project <gcp-project-name> ```

where "<cluster-name>" is the name of your GKE cluster, "<region>" and "<zone>" identify the GCP region zone in which
your cluster was created, and "<gcp-project-name>" is the name of your GCP project.

When your cluster credentials are set, create a GKE service:

``` kubectl run <service-name> --image gcr.io/<gcp-project-name>/k8s-token-gen:1.0-SNAPSHOT --port 8080 ```

where "<service-name>" is the name of your service and "<gcp-project-name>" is your GCP project name.  Once the service
is ready, expose it via a load balancer:

``` kubectl expose deployment <service-name> --type "LoadBalancer" ```

where "<service-name>" is the name of your service.


## Next steps

At this point your new GKE service is ready to be used by the Apigee API proxies.  From the root directory of this
repository, navigate to the "gateway" subdirectory and follow the directions in the README.md file.

Before you move forward, you may want to assign a DNS name to this service.  Use the appropriate domain registrar to
register a DNS name, and use the GCP Console to point the new DNS name to the IP address of your service.

