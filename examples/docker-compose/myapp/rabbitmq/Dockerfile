


#带有管理界面的rabbitmq
FROM rabbitmq:3.8.0-management-alpine
# 启动mqtt插件
RUN rabbitmq-plugins enable --offline rabbitmq_mqtt

COPY ./manually/testca/ca_certificate.pem /cert/ca_certificate.pem
COPY ./manually/server/server_certificate.pem /cert/server_certificate.pem
COPY ./manually/server/private_key.pem /cert/server_key.pem




