
### CURL
```
curl --location --request POST 'localhost:8080/api/v1/registration' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "Bashar",
    "lastName": "Naial",
    "email": "info@bashar.com",
    "password": "password"
}'
```
