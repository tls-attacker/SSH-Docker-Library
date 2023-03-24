version: '3.2'
services:
{%- if specs.capabilities.server -%}
  {%- for version in specs.serverVersions %}
  {{ specs.name }}-server-{{ version }}:
    image: rub-nds/{{ specs.name }}-server:{{ version }}
    build:
      context: .
      {%- if specs.multistage %}
      target: {{ specs.name }}-server
      {%- endif %}
      args:
        VERSION: {{ version }}
  {%- endfor -%}
{%- endif -%}
{%- if specs.capabilities.client -%}
  {%- for version in specs.clientVersions %}
  {{ specs.name }}-client-{{ version }}:
    image: rub-nds/{{ specs.name }}-client:{{ version }}
    build:
      context: .
      {%- if specs.multistage %}
      target: {{ specs.name }}-client
      {%- endif %}
      args:
        VERSION: {{ version }}
    profiles: [client]
  {%- endfor %}
{% endif %}
