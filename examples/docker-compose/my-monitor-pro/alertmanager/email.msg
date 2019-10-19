
{{ define "email.msg" }}
{{ range .Alerts }}
 <pre>
实例: {{ .Labels.instance }}
信息: {{ .Annotations.summary }}
详情: {{ .Annotations.description }}
时间: {{ .StartsAt.Format "2006-01-02 15:04:05" }}
 </pre>
{{ end }}
{{ end }}


