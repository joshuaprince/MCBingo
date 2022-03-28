FROM python:3.10.4
ENV PYTHONUNBUFFERED=1

WORKDIR /code
COPY ./requirements*.txt /code/
RUN pip install -r requirements_prod.txt

COPY . /code/backend/
RUN chmod +x /code/backend/docker-entrypoint.sh

WORKDIR /code/backend

EXPOSE 8000
VOLUME /static/
ENTRYPOINT ["./docker-entrypoint.sh"]
