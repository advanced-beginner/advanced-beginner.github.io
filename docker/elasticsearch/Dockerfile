FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Nori 한글 분석기 설치
RUN bin/elasticsearch-plugin install analysis-nori

# ICU 분석기 설치 (선택사항 - 유니코드 정규화)
# RUN bin/elasticsearch-plugin install analysis-icu
