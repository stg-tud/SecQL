#!/bin/bash
set -e

CLUSTER_NAME=i3ql-hospital-benchmark
NAMESPACE_NAME=hospital.i3ql
CIDR=10.0.1.0/24

VPC_ID=$(../lib/aws-ec2-get-vpcid.sh)
SERVER_REPO=i3ql-test-server
CLIENT_REPO=i3ql-test-client

echo "Creating cluster i3ql-hospital-benchmark"
aws ecs create-cluster\
	--cluster-name $CLUSTER_NAME

# Make sure cpu and memory combination exist in fargate
# https://docs.aws.amazon.com/de_de/cli/latest/reference/ecs/register-task-definition.html#options
../lib/aws-ecs-register-task-definition.sh i3ql-hospital-server $SERVER_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-hospital-patient $CLIENT_REPO 2vcpu 4GB
../lib/aws-ecs-register-task-definition.sh i3ql-hospital-person $CLIENT_REPO 2vcpu 4GB
../lib/aws-ecs-register-task-definition.sh i3ql-hospital-knowledge $CLIENT_REPO 2vcpu 4GB
../lib/aws-ecs-register-task-definition.sh i3ql-hospital-client $CLIENT_REPO 2vcpu 4GB


../lib/aws-ec2-create-subnet.sh $VPC_ID $CLUSTER_NAME $CIDR
SUBNET_ID=$(../lib/aws-ec2-get-subnetid.sh $CLUSTER_NAME)

../lib/aws-sd-create-namespace.sh $VPC_ID $NAMESPACE_NAME
NAMESPACE_ID=$(../lib/aws-sd-get-namespaceid.sh $NAMESPACE_NAME)

../lib/aws-create-service.sh $CLUSTER_NAME i3ql-hospital-server server $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-hospital-patient patient $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-hospital-person person $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-hospital-knowledge knowledge $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-hospital-client client $SUBNET_ID $NAMESPACE_ID