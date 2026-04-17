# Liquibase tutorial 1.0.0

## **Índice**
1. [Introdução](#introdução)
2. [Configuração Inicial](#configuração-inicial)
    - Dependências
    - Configuração no `application.properties`
3. [Estrutura de Pastas](#estrutura-de-pastas)
4. [Criação do Arquivo Master](#criação-do-arquivo-master)
5. [Tipos de Arquivos de Migração](#tipos-de-arquivos-de-migração)
    - YAML
    - JSON
    - XML
    - SQL
6. [Exemplo de Migração](#exemplo-de-migração)
7. [Execução e Validação](#execução-e-validação)
8. [Melhores Práticas](#melhores-práticas)

---

## **Introdução**
O **Liquibase** é uma ferramenta de versionamento de banco de dados que facilita a aplicação e controle de alterações de forma automatizada. Ele permite criar, alterar e versionar tabelas, colunas e registros, garantindo consistência e rastreabilidade.

---

## **Configuração Inicial**

### **1. Dependências**
Adicione a seguinte dependência no arquivo **`pom.xml`**:
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### **2. Configuração no `application.properties`**
````properties
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.enabled=true
````

## **Estrutura de Pastas**
Organize os arquivos de migração no diretório de recursos do projeto:
```css
src/
├── main/
│   ├── java/
│   ├── resources/
│       ├── db/
│       │   └── changelog/
│       │       ├── db.changelog-master.yaml
│       │       ├── db.changelog-1.0.yaml
│       │       ├── db.changelog-1.1.json
│       │       ├── db.changelog-1.2.xml
│       │       └── db.changelog-2.0.sql
│       ├── application.properties
│       └── application.yml
```

## **Criação do Arquivo Master**
O arquivo **`db.changelog-master.yaml`** serve como ponto de entrada para referenciar outros arquivos de migração.

### **Exemplo de Arquivo Master (YAML)**
````yaml
databaseChangeLog:
  - include:
      file: db/changelog/db.changelog-1.0.yaml
  - include:
      file: db/changelog/db.changelog-1.1.json
  - include:
      file: db/changelog/db.changelog-1.2.xml
  - include:
      file: db/changelog/db.changelog-2.0.sql
````

## **Tipos de Arquivos de Migração**
1. **YAML**
````yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: seu_nome
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: VARCHAR(255)
````
2. **JSON**
````json
{
  "databaseChangeLog": [
    {
      "changeSet": {
        "id": "1",
        "author": "seu_nome",
        "changes": [
          {
            "createTable": {
              "tableName": "users",
              "columns": [
                {
                  "column": {
                    "name": "id",
                    "type": "BIGINT",
                    "autoIncrement": true,
                    "constraints": { "primaryKey": true }
                  }
                },
                {
                  "column": {
                    "name": "name",
                    "type": "VARCHAR(255)",
                    "constraints": { "nullable": false }
                  }
                },
                {
                  "column": {
                    "name": "email",
                    "type": "VARCHAR(255)"
                  }
                }
              ]
            }
          }
        ]
      }
    }
  ]
}
````

3. **XML**

```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet id="1" author="seu_nome">
        <createTable tableName="users">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" />
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false" />
            </column>
            <column name="email" type="VARCHAR(255)" />
        </createTable>
    </changeSet>
</databaseChangeLog>
```

4. **SQL**
```sql
--liquibase formatted sql
-- changeset seu_nome:1
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);
```

## **Exemplo de Migração**

Adicionando uma coluna com valor padrão:

1. **YAML:**
````yaml
databaseChangeLog:
  - changeSet:
      id: 2
      author: seu_nome
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: status
                  type: VARCHAR(50)
                  defaultValue: 'active'
````

2. **SQL**
````sql
--liquibase formatted sql
-- changeset seu_nome:2
ALTER TABLE users ADD COLUMN status VARCHAR(50) DEFAULT 'active';
````

## Execução e Validação
1. **Aplicar Migrações Automáticas:**
   - Ao iniciar a aplicação Spring Boot, o Liquibase aplicará automaticamente as mudanças ainda não executadas.
2. **Verificar Histórico:**
   - O Liquibase mantém um histórico das migrações aplicadas na tabela **`DATABASECHANGELOG`**.
3. **Executar Manualmente:**
   - Você pode executar comandos como:
   ```bash
   mvn liquibase:update
   mvn liquibase:rollback -Dliquibase.rollbackCount=1
   ```
   
## **Melhores Práticas**
1. **Versionamento por Arquivo:**
   - Cada arquivo de migração deve representar uma versão incremental.
2. **Evitar Dependências de Ambiente:**
   - Use valores padrão ou scripts SQL genéricos.
3. **Backup do Banco:**
   - Realize backups antes de executar mudanças em produção.
4. **Testes Locais:**
   - Valide as migrações em um banco local antes de aplicá-las em produção.