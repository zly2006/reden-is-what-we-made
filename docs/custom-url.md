# Reden Custom URL

Reden added some custom url pattern for Minecraft's ClickEvent.

example:
```json
{ "text": "Hello", "clickEvent": { "action": "open_url", "value": "<pattern>" }}
```

| URL Pattern                   | Desc                                                         |
|-------------------------------|--------------------------------------------------------------|
| `reden:malilib:{key}={value}` | Set malilib config {key} to {value(string format)}           |
| `reden:stopClient`            | Shutdown the client, you can use it for client updating etc. |
