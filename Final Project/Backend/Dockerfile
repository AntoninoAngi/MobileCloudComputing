FROM python:3-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY python-flask-server/requirements.txt /usr/src/app/

ADD python-flask-server/requirements.txt requirements.txt
RUN pip install -r requirements.txt

COPY python-flask-server-generated/python-flask-server /usr/src/app

EXPOSE 8080

ENTRYPOINT ["python3"]

CMD ["-m", "swagger_server"]