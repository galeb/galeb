## Fake OAuth2 ##

```bash
cat <<EOF > nginx.conf
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html;
        index  index.json;
    }

    # To allow POST on static pages
    error_page 405 =200 $uri;

    error_page 500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
EOF


mkdir -p html/user
cat <<EOF > html/user/index.json
{
  "login": "user1",
  "id": 1,
  "url": "http://127.0.0.1:9000/users/user1",
  "type": "User",
  "name": "user1",
  "email": "test1@localhost",
  "created_at": "2008-01-14T04:33:35Z",
  "updated_at": "2008-01-14T04:33:35Z"
}
EOF

docker run --rm --name nginx -d -p 9000:80 -v $PWD/nginx.conf:/etc/nginx/conf.d/default.conf:ro -v $PWD/html:/usr/share/nginx/html:ro nginx:alpine

export OAUTH_USERINFO=http://127.0.0.1:9000/user/
```
