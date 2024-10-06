curl -X POST \
http://localhost/api/v1/chat/completions \
-H 'Content-Type:application/json;charset=utf-8' \
-H 'Authorization:eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6b21iaWUiLCJvcGVuSWQiOiJ6b21iaWUiLCJleHAiOjE3Mjg1NjgyNjgsImlhdCI6MTcyNzk2MzQ2OCwianRpIjoiYzA5ZGE2YzYtM2JjOC00NTVjLTlkZDItZDI2YzJkNzBjNzZlIn0.5apWLlnw4xpjJ8HmzxKJNgzMtv8n_jJl_VX_hS8_t9w' \
-d '{
"messages": [
{
"content": "帮我写一个java的冒泡排序",
"role": "user"
}
],
"model": "gpt-4"
}'