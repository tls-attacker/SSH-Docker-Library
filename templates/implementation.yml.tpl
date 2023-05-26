version: '3.2'
services:
{%- if specs.capabilities.server -%}
  {%- for version in specs.serverVersions %}
  {%- if version is string %}
  {{ specs.name }}-server-{{ version }}:
    image: rub-nds/{{ specs.name }}-server:{{ version }}
    build:
      context: .
      {%- if specs.multistage %}
      target: {{ specs.name }}-server
      {%- endif %}
      args:
        VERSION: {{ version }}
  {%- endif -%}
  {%- if version is mapping %}
  {{ specs.name }}-server-{{ version.version }}:
    image: rub-nds/{{ specs.name }}-server:{{ version.version }}
    build:
      context: .
      {% if version.target is defined %}
      target: {{ version.target }}
      {%- else -%}
      {%- if specs.multistage -%}
      target: {{ specs.name }}-server
      {%- endif -%}
      {%- endif %}
      {%- if version.dockerfile is defined %}
      dockerfile: {{ version.dockerfile }}
      {%- endif %}
      args:
        {%- if version.args is defined %}
        {%- for arg in version.args %}
        {{ arg.name }}: {{ arg.value }}
        {%- endfor -%}
        {%- else -%}
        VERSION: {{ version.version }}
        {%- endif %}
    {%- endif -%}
  {% endfor %}
{% endif %}
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
{%- if specs.capabilities.automatedclient -%}
  {%- for version in specs.clientVersions %}
  {{ specs.name }}-automated-client-{{ version }}:
    image: rub-nds/{{ specs.name }}-automated-client:{{ version }}
    build:
      context: .
      {%- if specs.multistage %}
      target: {{ specs.name }}-automated-client
      {%- endif %}
      args:
        VERSION: {{ version }}
    profiles: [automated-client]
  {%- endfor %}
{% endif %}
