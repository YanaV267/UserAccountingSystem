## User Accounting System

### Ссылка на Swagger: http://localhost:8080/swagger-ui.html

Перед началом работы необходимо:
 - запустить файл `docker-compose.yaml` при помощи команды `docker-compose up -d`
 - в файле `application.properties`  указать URL для Docker, который развёрнут у вас (для использования Elasticsearch и Redis):
![img.png](readme-images/img1.png)
 - запустить класс `WebApplication` 


База данных была заменена на Н2, потому что не было возможности использовать PostgreSQL (так как и БД, и Docker запущены в рабочей системе)


### Примеры начисления процентов на баланс каждые 30 секунд:
Изначальное начисление:
![img.png](readme-images/img2.png)

Достижение лимита по начислению на одном из счетов:
![img.png](readme-images/img3.png)

### Примеры запросов через Postman
![img.png](readme-images/img4.png)
![img.png](readme-images/img5.png)
![img.png](readme-images/img6.png)