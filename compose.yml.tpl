version: '3.2'
services:
{%- for portrange_start, (file_name, file_data) in enumerate(files.items(), start=220) -%}
{% for port, (service_name, service_data) in enumerate(file_data["services"].items(), start=portrange_start*100) %}
  {{ service_name }}:
    extends:
      file: {{ file_name }}
      service: {{ service_name }}
{%- if "client" not in service_data.get("profiles", []) %}
    ports:
      - '{{port}}:22'
{%- endif %}
{%- endfor %}
{% endfor %}
