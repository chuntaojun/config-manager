FROM ubuntu
RUN apt-get update \
    && apt-get install -y nodejs npm
COPY . /root/web
WORKDIR /root/web
EXPOSE 8077
RUN npm -g install http-server
CMD ["http-server", "-p", "8077 "]