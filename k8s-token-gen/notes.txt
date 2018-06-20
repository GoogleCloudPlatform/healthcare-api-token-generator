gcloud auth configure-docker (do once; from https://cloud.google.com/container-registry/docs/advanced-authentication)

gcloud container clusters create oauth-server-cluster

gcloud container clusters get-credentials oauth-server-cluster --zone us-west1-a --project cvs-healthcareplayground

kubectl run oauth-server --image gcr.io/cvs-healthcareplayground/k8s-token-gen:1.0-SNAPSHOT --port 8080
kubectl expose deployment oauth-server --type "LoadBalancer"



