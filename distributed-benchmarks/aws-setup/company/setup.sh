#!/bin/bash
set -e

CLUSTER_NAME=i3ql-company-benchmark
NAMESPACE_NAME=company.i3ql
CIDR=10.0.2.0/24

VPC_ID=$(../lib/aws-ec2-get-vpcid.sh)
SERVER_REPO=i3ql-test-server
CLIENT_REPO=i3ql-test-client

echo "Creating cluster $CLUSTER_NAME"
aws ecs create-cluster\
	--cluster-name $CLUSTER_NAME

# Make sure cpu and memory combination exist in fargate
# https://docs.aws.amazon.com/de_de/cli/latest/reference/ecs/register-task-definition.html#options
../lib/aws-ecs-register-task-definition.sh i3ql-company-server $SERVER_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-company-public $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-company-production $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-company-purchasing $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-company-employees $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-company-client $CLIENT_REPO 1vcpu 2GB


../lib/aws-ec2-create-subnet.sh $VPC_ID $CLUSTER_NAME $CIDR
SUBNET_ID=$(../lib/aws-ec2-get-subnetid.sh $CLUSTER_NAME)

../lib/aws-sd-create-namespace.sh $VPC_ID $NAMESPACE_NAME
NAMESPACE_ID=$(../lib/aws-sd-get-namespaceid.sh $NAMESPACE_NAME)

../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-server server $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-public public $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-production production $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-purchasing purchasing $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-employees employees $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-company-client client $SUBNET_ID $NAMESPACE_ID