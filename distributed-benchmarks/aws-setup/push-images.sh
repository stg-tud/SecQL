#!/bin/bash

$(aws ecr get-login --no-include-email --region us-east-1)

SERVER_REPO_NAME=i3ql-test-server
CLIENT_REPO_NAME=i3ql-test-client

aws ecr describe-repositories --repository-names $SERVER_REPO_NAME
if [ $? -ne 0 ]; then # Repository does not exist, so create it
	aws ecr create-repository --repository-name $SERVER_REPO_NAME
fi

SERVER_REPO=$(aws ecr describe-repositories --repository-names $SERVER_REPO_NAME | sed -nE '/"repositoryUri": "/{s/.*:\s*"(.*)",/\1/p;q}')
docker tag $SERVER_REPO_NAME:latest $SERVER_REPO:latest
docker push $SERVER_REPO:latest

aws ecr describe-repositories --repository-names $CLIENT_REPO_NAME
if [ $? -ne 0 ]; then # Repository does not exist, so create it
	aws ecr create-repository --repository-name $CLIENT_REPO_NAME
fi

CLIENT_REPO=$(aws ecr describe-repositories --repository-names $CLIENT_REPO_NAME | sed -nE '/"repositoryUri": "/{s/.*:\s*"(.*)",/\1/p;q}')
docker tag $CLIENT_REPO_NAME:latest $CLIENT_REPO:latest
docker push $CLIENT_REPO:latest