FROM docker.elastic.co/elasticsearch/elasticsearch:7.7.1

# copy config 
COPY config.properties /usr/share/elasticsearch/data

# copy plugin file
COPY target/releases/elasticsearch-analysis-baikal-7.7.1.zip /usr/share/elasticsearch/data

WORKDIR /usr/share/elasticsearch
# install plugin
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///usr/share/elasticsearch/data/elasticsearch-analysis-baikal-7.7.1.zip
