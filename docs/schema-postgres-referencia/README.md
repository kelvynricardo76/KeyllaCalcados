# Schema Postgres (referência histórica)

Estes scripts SQL eram as migrations Flyway originais do projeto, escritas
para PostgreSQL (tipos ENUM nativos, índices únicos parciais, `JSONB`, etc.).

O backend migrou para **H2 em arquivo local com schema gerenciado pelo
Hibernate** (`ddl-auto: update`), para rodar sem nenhuma dependência externa
(sem Docker, sem Postgres). Por isso esses arquivos não são mais executados
pela aplicação — ficam aqui só como referência do desenho original do banco,
caso o projeto volte a rodar sobre Postgres no futuro (bastaria reintroduzir
o Flyway, apontar o `datasource` para Postgres e adaptar os enums/índices que
hoje são simplificados nas entidades JPA).
