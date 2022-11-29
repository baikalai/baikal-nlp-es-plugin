# baikal-nlp-es-plugin
Elastic search plugin for BAIKAL pos tagger



# config file
"/usr/share/elasticsearch/data/config.properties"
```
NLP_server_address = localhost
NLP_server_port = 5656
stop_tokens = E,IC,J,MAG,MAJ,MM,SP,SSC,SSO,SC,SE,XPN,XSA,XSN,XSV,UNA,NA,VSV
```



# install elastic
- 도커 방식의 8.5 설치 https://www.elastic.co/guide/en/elasticsearch/reference/8.5/docker.html
- 7.7.1 설치 https://www.elastic.co/guide/en/elasticsearch/reference/7.7/docker.html
- nori 설치 https://anygyuuuu.tistory.com/13
- 키바나 설치 https://www.elastic.co/guide/en/kibana/7.7/deb.html#deb-repo


- 엘라스틱서치 커스텀 플러그인을 작성하고 빌드하는 방법(with. gradle) https://velog.io/@yaincoding/%EC%97%98%EB%9D%BC%EC%8A%A4%ED%8B%B1%EC%84%9C%EC%B9%98-%EC%BB%A4%EC%8A%A4%ED%85%80-%ED%94%8C%EB%9F%AC%EA%B7%B8%EC%9D%B8%EC%9D%84-%EC%9E%91%EC%84%B1%ED%95%98%EA%B3%A0-%EB%B9%8C%EB%93%9C%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95with.-gradle


# 한글 형태소 품사 (Part Of Speech, POS) 태그표
http://kkma.snu.ac.kr/documents/index.jsp?doc=postag



# docker file

vi Dockerfile
----------------------------
FROM docker.elastic.co/elasticsearch/elasticsearch:7.7.1

# copy config 
COPY config.properties /usr/share/elasticsearch/data

# copy plugin file
COPY target/releases/elasticsearch-analysis-baikal-7.7.1.zip /usr/share/elasticsearch/data

WORKDIR /usr/share/elasticsearch
# install plugin
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///usr/share/elasticsearch/data/elasticsearch-analysis-baikal-7.7.1.zip
----------------------------

docker build -t elasticsearch-with-baikal-nlp:7.7.1 .
docker login
docker push elasticsearch-with-baikal-nlp:7.7.1


# docker 실행

docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch-with-baikal-nlp:7.7.1
