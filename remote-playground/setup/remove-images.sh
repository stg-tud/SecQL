#!/bin/bash

$(aws ecr get-login --no-include-email --region us-east-1)

SERVER_REPO_NAME=i3ql-test-server
CLIENT_REPO_NAME=i3ql-test-client

aws ecr describe-repositories --repository-names $SERVER_REPO_NAME
if [ $? -eq 0 ]; then # Repository exists, so delete it
	aws ecr delete-repository --repository-name $SERVER_REPO_NAME
fi

aws ecr describe-repositories --repository-names $CLIENT_REPO_NAME
if [ $? -eq 0 ]; then # Repository exists, so delete it
	aws ecr delete-repository --repository-name $CLIENT_REPO_NAME
fi