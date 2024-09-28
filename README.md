# chatgpt-data | DDD 工程分层架构

## 测试脚本

### 1. 验证码

```java
curl -X POST \
 http://localhost:8091/api/v1/auth/gen/code \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'openid=xfg'
```

- 也可以通过启动本地 natapp 内网穿透，对接公众号进行获取验证码

### 2. 登录 - 获取 Token

```java
curl -X POST \
http://localhost/api/v1/auth/login \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'code=6462'
```

- 登录后可以获取 Token

### 3. 功能 - 流式问题

```java
curl -X POST \
http://localhost/api/v1/chat/completions \
-H 'Content-Type: application/json;charset=utf-8' \
-H 'Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6b21iaWUiLCJvcGVuSWQiOiJ6b21iaWUiLCJleHAiOjE3MjgwNDY4NTMsImlhdCI6MTcyNzQ0MjA1MywianRpIjoiZmUwNTk2N2ItMDhhNi00ZWEzLThkZDYtMzgwMTRlYzI3MTQ4In0.4kn-PzbClC_Ngc7czDt5uvPZVsgRGW1I0_yjfua75qc' \
-d '{
"messages": [
{
"content": "1+1",
"role": "user"
}
],
"model": "glm-3-turbo2"
}'
```

- Token 是通过登录从控制台复制的，注意可别复制错了。