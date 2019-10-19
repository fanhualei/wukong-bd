{{ define "wechat.msg" }}{{- if gt (len .Alerts.Firing) 0 -}}{{ range .Alerts }}
@警报
状态:{{ .Status }}
级别:{{ .Labels.severity }}
类型:{{ .Labels.alertname }}
标题:{{ .Annotations.summary }}
详情:{{ .Annotations.description }}
触发时间:{{ .StartsAt.Format "2006-01-02 15:04:05" }}
{{ end }}{{ end -}}{{- if gt (len .Alerts.Resolved) 0 -}}{{ range .Alerts }}
@恢复
状态:{{ .Status }}
级别:{{ .Labels.severity }}
类型:{{ .Labels.alertname }}
标题:{{ .Annotations.summary }}
详情:{{ .Annotations.description }}
触发时间:{{ .StartsAt.Format "2006-01-02 15:04:05" }}
恢复时间: {{ .EndsAt.Format "2006-01-02 15:04:05" }}
{{ end }}{{ end -}}{{- end }}
