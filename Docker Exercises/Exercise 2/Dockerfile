FROM alpine:3.10
WORKDIR /application

ARG ANDROID_SDK_TOOLS=4333796

ENV ANDROID_HOME /usr/local/android
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV SDK_TOOLS_VERSION sdk-tools-linux-4333796
ENV PATH $PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin
ENV ANDROID_SDK_VERSION 26
ENV ANDROID_BUILD_TOOLS_VERSION 26.0.3


RUN apk --update add openjdk8-jre openjdk8

# SDK
 RUN apk --no-cache add bash wget unzip git
 RUN wget -q -O android-sdk.zip https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS}.zip
 RUN mkdir ${ANDROID_HOME}
 RUN unzip -qo android-sdk.zip -d ${ANDROID_HOME}
 RUN chmod +x ${ANDROID_HOME}/tools/android
 RUN rm android-sdk.zip && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"

# Install Android Build Tool and Libraries
RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN touch /root/.android/repositories.cfg
RUN $ANDROID_HOME/tools/bin/sdkmanager  "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
RUN $ANDROID_HOME/tools/bin/sdkmanager  "platform-tools"
RUN $ANDROID_HOME/tools/bin/sdkmanager  "platforms;android-26"


RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk -O /tmp/glibc.apk && \
wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-bin-2.30-r0.apk -O /tmp/glibc-bin.apk && \
apk add --no-cache /tmp/glibc.apk /tmp/glibc-bin.apk && \
rm -rf /tmp/* && \
rm -rf /var/cache/apk/*

