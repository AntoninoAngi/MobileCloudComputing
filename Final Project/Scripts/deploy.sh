#!/bin/bash

cd ..

mkdir OUTPUT_DIR
OUTPUT_DIR='./OUTPUT_DIR/'

# Functions for customizing colors(Optional)
print_red(){
    printf "\e[1;31m$1\e[0m"
}
print_green(){
    printf "\e[1;32m$1\e[0m"
}
print_yellow(){
    printf "\e[1;33m$1\e[0m"
}
print_blue(){
    printf "\e[1;34m$1\e[0m"
}

#Start Build Process
print_blue "\n\n\nStarting"
print_blue "\n\n\nCleaning...\n"
./gradlew clean

print_blue "\n\n\ncleanBuildCache...\n"
./gradlew cleanBuildCache

print_blue "\n\n\n build...\n"
./gradlew build

print_blue "\n\n\n assembleDebug...\n"
./gradlew assembleDebug

#Install APK on device / emulator
#print_blue "installDebug...\n"
#./gradlew installDebug

print_blue "\n\n\n Done Installing\n"

#Launch Main Activity
#adb shell am start -n "com.gmail.albertosilveiramos.mcc_fall_2019_g01/.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

#print_blue "\n\n\n Launched main activity\n"

#Copy APK to output folder
cp "app/build/outputs/apk/debug/app-debug.apk" $OUTPUT_DIR
print_blue "\n\n\n Copying APK to 'apk' folder Done\n"

print_blue "\n\n\n Starting with the backend...\n"

gcloud init

gcloud auth login

gcloud config set project mcc-fall-2019-g01-258815

cd Backend/python-flask-server-generated/python-flask-server/swagger_server/swagger

print_blue "\n\n\n Deploying the API...\n"

gcloud endpoints services deploy swagger.yaml

print_blue "\n\n\n Deploying the APP ENGINE...\n"

cd ../..

gcloud app deploy

print_blue "\n\n\n Deployment completed \n"
