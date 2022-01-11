## /token/create

```
POST /token/create

{
  "serial": string,
  "username": string?,
  "isCamera": boolean?,
}
```

## /photo

```
PUT /photo/`cameraToken`

{
  "token": string, // MUST BE user token
  "photo": file, // MUST BE jpeg
}
```