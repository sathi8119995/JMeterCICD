# Use a lightweight Java image
FROM azul/zulu-openjdk:11

# JMeter version
ARG JMETER_VERSION=5.6.3

# Install utilities (wget to download JMeter, unzip/tar to extract)
RUN apt-get update && \
    apt-get install -y curl tar && \
    rm -rf /var/lib/apt/lists/*

# Download and unpack JMeter
WORKDIR /opt
RUN curl -fSL --retry 5 --retry-delay 5 https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz -o apache-jmeter-${JMETER_VERSION}.tgz && \
    tar -xzf apache-jmeter-${JMETER_VERSION}.tgz && \
    rm apache-jmeter-${JMETER_VERSION}.tgz && \
    mv apache-jmeter-${JMETER_VERSION} jmeter

# Set JMeter Home and add bin to PATH
ENV JMETER_HOME /opt/jmeter
ENV PATH $JMETER_HOME/bin:$PATH

# Set working directory for tests
WORKDIR /jmeter

# Entrypoint: This allows arguments passed in 'docker run' to be passed to the jmeter command
ENTRYPOINT ["jmeter"]