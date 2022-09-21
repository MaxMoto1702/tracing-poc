FROM python:3.7.2-slim-stretch

ENV WORKDIR='/unikoom-ds'

WORKDIR $WORKDIR
COPY . $WORKDIR/
RUN pip install --no-cache-dir -r requirements.txt
RUN python ./integration/app_version.py

EXPOSE 3030
ENTRYPOINT [ "./entrypoint.sh" ]
