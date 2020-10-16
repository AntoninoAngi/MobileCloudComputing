#!/bin/bash

cd ../swagger

gcloud init 
gcloud auth login
gcloud config set project mcc-fall-2019-g01-258815
gcloud endpoints services deploy swagger.yaml


