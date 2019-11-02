local welcome = 'Hello lua'

local aaa = ngx.var.remote_addr

ngx.say(aaa)


ngx.say(ngx.var.remote_addr)


ngx.say(ngx.var.uri)


ngx.say(ngx.var.remote_addr)


    local request_host = ngx.var.host
    local request_uri = ngx.unescape_uri(ngx.var.uri)
    local request_status = ngx.var.status
    local request_scheme = ngx.var.scheme
    local request_method = ngx.var.request_method
    local remote_ip = ngx.var.remote_addr
    local ngx_sent = ngx.var.body_bytes_sent
    local latency = ngx.var.upstream_response_time or 0
ngx.say("-----------------------")

ngx.say("host:",request_host)
ngx.say("uri:",request_uri)
ngx.say("status:",request_status)
ngx.say("scheme:",request_scheme)
ngx.say("method:",request_method)
ngx.say("ip:",remote_ip)
ngx.say("sent:",ngx_sent)
ngx.say("latency:",latency)

ngx.say("---------------")




